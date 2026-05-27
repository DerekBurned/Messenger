 

import * as admin from "firebase-admin";
import {logger} from "firebase-functions";
import {setGlobalOptions} from "firebase-functions/v2";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {onValueCreated} from "firebase-functions/v2/database";

admin.initializeApp();

setGlobalOptions({region: "europe-west1"});

const db = admin.firestore();
const messaging = admin.messaging();

interface ConversationDoc {
  participantIds?: string[];
  participantNames?: string[];
}

interface MessageDoc {
  senderId?: string;
  text?: string;
  timestamp?: number;
}

interface UserDoc {
  username?: string;
  fcmToken?: string;
}

export const onMessageCreated = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;
    const message = snapshot.data() as MessageDoc;
    const conversationId = event.params.conversationId;
    const messageId = event.params.messageId;
    const senderId = message.senderId ?? "";
    if (!senderId) {
      logger.warn("Skipping message with no senderId", {conversationId, messageId});
      return;
    }

    const convRef = db.collection("conversations").doc(conversationId);
    const convSnap = await convRef.get();
    const conversation = (convSnap.data() ?? {}) as ConversationDoc;
    const recipients = (conversation.participantIds ?? []).filter((id) => id !== senderId);
    if (recipients.length === 0) return;

    const [senderSnap, ...recipientSnaps] = await Promise.all([
      db.collection("users").doc(senderId).get(),
      ...recipients.map((uid) => db.collection("users").doc(uid).get()),
    ]);
    const sender = (senderSnap.data() ?? {}) as UserDoc;
    const senderName = sender.username ?? "New message";

    const tokens = recipientSnaps
      .map((s) => (s.data() as UserDoc | undefined)?.fcmToken)
      .filter((t): t is string => typeof t === "string" && t.length > 0);
    if (tokens.length === 0) {
      logger.info("No FCM tokens for recipients", {conversationId, recipients});
      return;
    }

    const preview = (message.text ?? "").slice(0, 200);
    const timestamp = (message.timestamp ?? Date.now()).toString();

    const response = await messaging.sendEachForMulticast({
      tokens,
      data: {
        type: "message",
        conversationId,
        messageId,
        senderId,
        senderName,
        preview,
        timestamp,
      },
      android: {priority: "high"},
    });

    if (response.failureCount > 0) {
      response.responses.forEach((r, i) => {
        if (!r.success) {
          logger.warn("FCM send failed", {token: tokens[i], error: r.error?.message});
        }
      });
    }
  },
);

interface CallSignalDoc {
  callId?: string;
  callerId?: string;
  calleeId?: string;
  channelName?: string;
  status?: string;
}

export const onCallSignalCreated = onValueCreated(
  "/calls/{calleeId}/{callId}",
  async (event) => {
    const signal = (event.data.val() ?? {}) as CallSignalDoc;
    if (signal.status !== "RINGING") return;
    const calleeId = event.params.calleeId;
    const callId = signal.callId ?? event.params.callId;
    const callerId = signal.callerId ?? "";
    const channelName = signal.channelName ?? "";
    if (!calleeId || !callId || !channelName) return;

    const [calleeSnap, callerSnap] = await Promise.all([
      db.collection("users").doc(calleeId).get(),
      callerId ? db.collection("users").doc(callerId).get() : Promise.resolve(null),
    ]);
    const callee = (calleeSnap.data() ?? {}) as UserDoc;
    const token = callee.fcmToken;
    if (!token) {
      logger.info("No FCM token for callee — cannot wake device", {calleeId});
      return;
    }
    const callerName = (callerSnap?.data() as UserDoc | undefined)?.username ?? callerId;

    await messaging.send({
      token,
      data: {
        type: "call",
        callId,
        callerId,
        callerName,
        channelName,
      },
      android: {priority: "high"},
    });
  },
);
