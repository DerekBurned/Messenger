# Messenger — Security & Code Quality Audit

Date: 2026-06-23
Branch: `UI_remake`
Reviewer: static code review (read-only). No dynamic testing / pentest was performed.

## Scope & method

Static review of the Android client, Firebase Security Rules (Firestore / Realtime Database / Storage), Cloud Functions, the Gradle build, and the manifest. Findings cite `file:line`.

**Important caveat:** Only the rules files *committed to the repo* were reviewed. The **deployed** Firebase rules could not be verified and may differ (see SEC-02). Treat rule findings as "what the repo says"; confirm against the live project in the Firebase console.

## Severity summary

| ID | Severity | Area | Title |
|----|----------|------|-------|
| SEC-01 | High | Storage rules | Committed `storage.rules` don't cover the paths the app actually uses |
| SEC-02 | High | Firestore | Any authenticated user can read every user's PII + FCM token |
| SEC-03 | Medium | Firestore | Message `update` rule permits tampering with other users' messages |
| SEC-04 | Medium | Calls | Agora joins with an empty token (no auth/expiry); App ID committed |
| SEC-05 | Medium | Build | Release build is signed with the debug keystore |
| SEC-06 | Medium | Platform | Firebase App Check not enabled |
| SEC-07 | Low | RTDB | `presence` / `typing` readable by any authenticated user; call-spam vector |
| SEC-08 | Low | Logging | Identifiers (uids, channel names) logged at `Log.e` in release |
| PERF-01 | Medium | Data | `upsert()` runs 2 DB queries per conversation on every snapshot |
| PERF-02 | Medium | Data | ObjectBox disk I/O not confined to `Dispatchers.IO` |
| PERF-03 | Low | Data | Read-state merge re-queries all cached messages per realtime emit |
| PERF-04 | Low | Compose | `enum.values()` + lambda allocations in composition |
| QUAL-01 | Low | Compose | Deprecated (non-AutoMirrored) icons |
| QUAL-02 | Low | Errors | `catch { Result.failure }` swallows errors without logging |
| QUAL-03 | Info | Product | Chat header no longer exposes a way to start a call |

---

## Security

### SEC-01 (High) — `storage.rules` do not match the paths the app uses
`storage.rules` only allows `avatars/{userId}/{fileName}` and denies everything else ([storage.rules:5,14](storage.rules)). But the app uploads to:
- `profileImages/{uid}/{uuid}.jpg` — [FirebaseStorageService.kt:15,20](app/src/main/java/com/example/messenger/data/remote/firebase/FirebaseStorageService.kt:15)
- `chatMedia/{conversationId}/{messageId}/{itemId}.{ext}` — [MediaRepositoryImpl.kt:239](app/src/main/java/com/example/messenger/data/repository/MediaRepositoryImpl.kt:239)

Neither path is `avatars/`. With the committed rules deployed, profile-image and chat-media upload/download would all be denied. Since those features work, the **deployed Storage rules must differ from the repo** — i.e. they are unreviewed and, given the catch-all here is a deny, the live ones are very likely permissive (e.g. `allow read, write: if request.auth != null` on everything). That would let any authenticated user read or overwrite **any conversation's media** by guessing/he harvesting the `chatMedia/{conversationId}/...` path.

Fix:
- Commit the real rules so they are reviewable and version-controlled.
- Lock `chatMedia/**` to conversation participants (or at minimum authed read + path-owner write) with size and `contentType` limits like the avatars block already has.
- Reconcile `avatars/` vs `profileImages/` (one is dead).

### SEC-02 (High) — Any authenticated user can read every user document
```
match /users/{userId} { allow read: if isAuthenticated(); ... }
```
[firestore.rules:90-91](firestore.rules). The user document carries PII and the FCM token (`email`, `phoneNumber`, `fcmToken`, `avatarUrl`). Any signed-in account can therefore enumerate every user's email and phone number and harvest FCM tokens.

Fix:
- Move `fcmToken` (and ideally `email`/`phoneNumber`) into an owner-only private subcollection (`users/{uid}/private/...`) readable only by `isOwner(uid)`.
- For user search, expose only the minimal public fields, or perform search through a Cloud Function rather than open document reads.

### SEC-03 (Medium) — Message `update` rule allows editing other users' messages
[firestore.rules:150-158](firestore.rules) permits any participant to update any message as long as `senderId`/`conversationId` are unchanged and `deletedFor` toggles only their own uid. It does **not** pin `text`, `mediaItems`, or `timestamp`, so participant B can rewrite participant A's message content. (The rule is permissive because the recipient must be able to set `status = READ` on the sender's message.)

Fix: on update, require immutable content fields, e.g.
```
request.resource.data.text == resource.data.text &&
request.resource.data.get('mediaItems', []) == resource.data.get('mediaItems', []) &&
request.resource.data.timestamp == resource.data.timestamp
```
and only allow `status` to move forward + own `deletedFor`.

### SEC-04 (Medium) — Agora joins with an empty token; App ID committed
`engine.joinChannel("", channelName, uid, options)` — [AgoraCallService.kt:122](app/src/main/java/com/example/messenger/data/remote/call/AgoraCallService.kt:122). App-ID-only mode has no per-channel authorization, no expiry, and no revocation. Channel names are random UUIDs ([CallViewModel.kt:149](app/src/main/java/com/example/messenger/presentation/viewmodel/CallViewModel.kt:149)) which mitigates casual eavesdropping, but the channel name is also written to the log ([AgoraCallService.kt:121](app/src/main/java/com/example/messenger/data/remote/call/AgoraCallService.kt:121)) and the App ID ships in the APK with a real-looking fallback committed in the build script ([build.gradle.kts:27](app/build.gradle.kts:27)).

Fix: enable the Agora App Certificate and issue short-lived, per-channel, per-uid RTC tokens from a trusted server (Cloud Function); pass the token to `joinChannel`. Remove the committed fallback App ID.

### SEC-05 (Medium) — Release is signed with the debug keystore
```
release { signingConfig = signingConfigs.getByName("debug") ... }
```
[build.gradle.kts:51](app/build.gradle.kts:51). The debug key is shared and well-known, so a release built this way has no authentic publisher identity and cannot be safely distributed.

Fix: add a real `release` signing config sourced from a keystore supplied via Gradle properties / env vars (never committed). `*.jks`/`*.keystore` are already gitignored — good.

### SEC-06 (Medium) — Firebase App Check not enabled
No App Check initialization was found. Without it the Firestore/RTDB/Storage/Functions backends accept requests from any client holding the (extractable) config, enabling scraping and abuse outside the app — this compounds SEC-02.

Fix: enable App Check with the Play Integrity provider and enforce it on all Firebase products.

### SEC-07 (Low) — RTDB reads not scoped to participants; call-spam vector
- `presence/$userId` ([database.rules.json:5](database.rules.json)) and `typing/$conversationId` ([database.rules.json:21](database.rules.json)) are readable by any authenticated user.
- Any authenticated user may write a call signal to any callee ([database.rules.json:36](database.rules.json)), i.e. ring arbitrary users.

RTDB can't easily consult Firestore participants, so some of this is an accepted trade-off. Consider treating presence as intentionally public, and add rate-limiting / a block-list for call initiation.

### SEC-08 (Low) — Verbose identifier logging in release
`AgoraCallService` uses `Log.e` for normal lifecycle events including channel names and uids; `FirestoreService` logs uids (`AUTHFLOW_FS`). These reach logcat in release.

Fix: gate logs behind `BuildConfig.DEBUG` (or a Timber release tree) and strip `Log.*` via ProGuard in release.

---

## Performance / optimization

### PERF-01 (Medium) — `upsert()` does 2 DB queries per conversation per snapshot
In `getAllConversations`, every Firestore snapshot runs `sync.conversations.forEach { upsert(it) }` ([ConversationRepositoryImpl.kt:41](app/src/main/java/com/example/messenger/data/repository/ConversationRepositoryImpl.kt:41)), and each `upsert` runs `findByUid()` + `localUnread()` (an ObjectBox count) ([ConversationRepositoryImpl.kt:244-258](app/src/main/java/com/example/messenger/data/repository/ConversationRepositoryImpl.kt:244)). That is 2N queries for N conversations on **every** snapshot, with no diffing.

Fix: react to `snapshot.documentChanges` and upsert only changed documents; batch writes.

### PERF-02 (Medium) — ObjectBox disk I/O not on `Dispatchers.IO`
Repository `suspend` functions call ObjectBox `find()/put()/count()` directly (e.g. `markConversationAsRead`, `syncConversations`, `findByUid`, `prune`, `upsert`). If invoked from the main dispatcher these block the UI thread on disk I/O.

Fix: wrap DB access in `withContext(Dispatchers.IO)`.

### PERF-03 (Low) — Read-state merge re-queries all cached messages per emit
`putPreservingReadState` loads every cached message for the conversation on each realtime emit ([MessageRepositoryImpl.kt](app/src/main/java/com/example/messenger/data/repository/MessageRepositoryImpl.kt)). Bounded by the cache window, but O(n) per emit.

Fix: query only `isRead == true` uids (or keep an in-memory read-set) instead of loading full rows.

### PERF-04 (Low) — Allocation in composition
`MediaTab.values()` allocates a new array on each recomposition ([ChatUserProfileScreen.kt](app/src/main/java/com/example/messenger/presentation/screens/ChatUserProfileScreen.kt) `MediaTabsRow`). Prefer `MediaTab.entries`. Several per-item lambdas in `ChatScreen` also allocate but are minor.

---

## Code quality / correctness

### QUAL-01 (Low) — Deprecated icons
Non-AutoMirrored `Icons.Filled.CallMissed` / `Icons.Filled.Reply` ([ChatScreen.kt:935](app/src/main/java/com/example/messenger/presentation/screens/ChatScreen.kt:935), [MessageContextDialog.kt:171,209](app/src/main/java/com/example/messenger/presentation/screens/MessageContextDialog.kt:171)). Switch to the `Icons.AutoMirrored.*` variants for RTL correctness.

### QUAL-02 (Low) — Errors swallowed silently
Many `catch (e) { Result.failure(e) }` blocks discard the cause without logging, making field failures hard to diagnose. Add at least a `Log.w(TAG, ..., e)` on the failure paths.

### QUAL-03 (Info) — No call entry in the chat header
The chat header redesign removed the call button (an intentional decision this session), so there is currently no in-chat way to start a call. Make sure a call affordance exists elsewhere (e.g. the user-profile screen, which does have a Call button).

---

## Things done right

- `google-services.json`, `*.jks`/`*.keystore`, and `local.properties` are gitignored.
- Cleartext traffic is disabled (`network_security_config.xml`: `cleartextTrafficPermitted="false"`).
- `allowBackup="false"`; sensitive exported surface is minimal (only `MainActivity` is exported, with no custom deep-link filters).
- Firestore rules use good owner/participant helper functions and field-scoped guards (`onlyTouchesOwnUnreadCount`, `onlyTogglesOwnClearedAt`, etc.); message create validation is solid.
- The avatars Storage block is a good template (owner write, size cap, `image/*` content-type) — extend the same pattern to the other paths.

## Suggested remediation order

1. SEC-01 and SEC-02 — confirm and lock down the deployed Storage rules and user-document reads (highest data-exposure risk).
2. SEC-05 — real release signing before any distribution.
3. SEC-03, SEC-04, SEC-06 — message-field pinning, Agora token auth, App Check.
4. PERF-01 / PERF-02 — snapshot diffing and `Dispatchers.IO` for DB work.
5. Remaining Low / Info items as cleanup.
