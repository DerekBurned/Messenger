package com.example.messenger.data.crypto

import com.example.messenger.data.local.obx.ObxPeerKey
import com.example.messenger.data.local.obx.ObxPeerKey_
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.objectbox.Box
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class PeerKey(val uid: String, val publicKey: ByteArray, val epoch: Int, val verified: Boolean)

@Singleton
class PeerKeyRegistry @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val peerKeyBox: Box<ObxPeerKey>,
    private val identityKeyStore: IdentityKeyStore,
) {

    suspend fun publishIfNeeded(myUid: String) {
        val identity = identityKeyStore.getOrCreate(myUid)
        val doc = keysCollection(myUid).document(identity.epoch.toString())
        val snapshot = runCatching { doc.get().await() }.getOrNull()
        val localPub = b64(identity.publicKey)
        if (snapshot?.getString("pub") == localPub) return
        doc.set(
            mapOf(
                "pub" to localPub,
                "epoch" to identity.epoch,
                "algo" to "X25519",
                "updatedAt" to System.currentTimeMillis(),
            ),
        ).await()
    }

    suspend fun ensurePeerKey(uid: String): PeerKey? {
        val cached = latestLocal(uid)
        if (cached != null && System.currentTimeMillis() - cached.seenAt < REFRESH_MS) {
            return cached.toPeerKey()
        }
        val snapshot = runCatching {
            keysCollection(uid).orderBy("epoch", Query.Direction.DESCENDING).limit(1).get().await()
        }.getOrNull()
        val doc = snapshot?.documents?.firstOrNull() ?: return cached?.toPeerKey()
        val pub = doc.getString("pub") ?: return cached?.toPeerKey()
        val epoch = doc.getLong("epoch")?.toInt() ?: return cached?.toPeerKey()
        return store(uid, epoch, pub).toPeerKey()
    }

    suspend fun peerKeyForEpoch(uid: String, epoch: Int): PeerKey? {
        localRow(uid, epoch)?.let { return it.toPeerKey() }
        val doc = runCatching { keysCollection(uid).document(epoch.toString()).get().await() }
            .getOrNull() ?: return null
        val pub = doc.getString("pub") ?: return null
        return store(uid, epoch, pub).toPeerKey()
    }

    fun cachedPeerKey(uid: String, epoch: Int): PeerKey? = localRow(uid, epoch)?.toPeerKey()

    fun latestPeerKey(uid: String): PeerKey? = latestLocal(uid)?.toPeerKey()

    fun setVerified(uid: String, verified: Boolean) {
        val row = latestLocal(uid) ?: return
        row.verified = verified
        peerKeyBox.put(row)
    }

    private fun store(uid: String, epoch: Int, pub: String): ObxPeerKey {
        val existing = localRow(uid, epoch)
        val row = ObxPeerKey(
            boxId = existing?.boxId ?: 0,
            uidEpoch = "$uid:$epoch",
            uid = uid,
            epoch = epoch,
            publicKey = pub,
            verified = existing?.verified ?: false,
            seenAt = System.currentTimeMillis(),
        )
        peerKeyBox.put(row)
        return row
    }

    private fun localRow(uid: String, epoch: Int): ObxPeerKey? =
        peerKeyBox.query(ObxPeerKey_.uidEpoch.equal("$uid:$epoch")).build().use { it.findFirst() }

    private fun latestLocal(uid: String): ObxPeerKey? =
        peerKeyBox.query(ObxPeerKey_.uid.equal(uid)).orderDesc(ObxPeerKey_.epoch).build()
            .use { it.findFirst() }

    private fun keysCollection(uid: String) =
        firestore.collection("users").document(uid).collection("keys")

    private fun ObxPeerKey.toPeerKey() = PeerKey(uid, unb64(publicKey), epoch, verified)

    companion object {
        private const val REFRESH_MS = 30 * 60 * 1000L
    }
}
