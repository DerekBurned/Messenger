package com.example.messenger.data.crypto

import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.dto.RemoteMessageDto
import com.example.messenger.data.remote.dto.toDomain
import com.example.messenger.data.remote.dto.toRemoteDto
import com.example.messenger.domain.model.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class E2eeMessageCodec @Inject constructor(
    private val identityKeyStore: IdentityKeyStore,
    private val peerKeyRegistry: PeerKeyRegistry,
    private val authService: FirebaseAuthService,
    private val participantResolver: ParticipantResolver,
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun isEncryptable(conversationId: String): Boolean {
        val myUid = authService.getCurrentUserId() ?: return false
        val partnerId = participantResolver.partnerOf(conversationId, myUid) ?: return false
        return peerKeyRegistry.ensurePeerKey(partnerId) != null
    }

    suspend fun encode(message: Message): RemoteMessageDto {
        val plain = message.toRemoteDto()
        if (message is Message.Call) return plain
        val myUid = authService.getCurrentUserId() ?: return plain
        val partnerId = participantResolver.partnerOf(message.conversationId, myUid) ?: return plain
        val peer = peerKeyRegistry.ensurePeerKey(partnerId) ?: return plain
        val identity = identityKeyStore.getOrCreate(myUid)
        val payload = when (message) {
            is Message.Text -> MessagePayload(text = message.text, replyToText = message.replyToText)
            is Message.Media -> MessagePayload(
                text = message.caption,
                replyToText = message.replyToText,
            )
            is Message.Call -> return plain
        }
        val key = ConversationCrypto.deriveConversationKey(
            identity.privateKey, peer.publicKey, myUid, partnerId, identity.epoch, peer.epoch,
        )
        val aad = messageAad(plain.conversationId, plain.id, plain.senderId, plain.type)
        val blob = ConversationCrypto.encrypt(key, json.encodeToString(payload).toByteArray(), aad)
        return plain.copy(
            text = "",
            replyToText = null,
            mediaItems = plain.mediaItems.map { it.copy(blurHash = "") },
            enc = 1,
            ciphertext = b64(blob.ciphertext),
            nonce = b64(blob.nonce),
            senderEpoch = identity.epoch,
            recipientEpoch = peer.epoch,
            recipientId = partnerId,
        )
    }

    suspend fun decode(dto: RemoteMessageDto, hasPendingWrites: Boolean = false): Message {
        if (dto.enc != 1) return dto.toDomain(hasPendingWrites)
        val payload = runCatching { decryptPayload(dto) }.getOrNull()
            ?: return undecryptable(dto, hasPendingWrites)
        val message = dto.copy(text = payload.text, replyToText = payload.replyToText)
            .toDomain(hasPendingWrites)
        return message
    }

    private suspend fun decryptPayload(dto: RemoteMessageDto): MessagePayload {
        val myUid = authService.getCurrentUserId() ?: error("not signed in")
        val iAmSender = dto.senderId == myUid
        val partnerId = if (iAmSender) dto.recipientId else dto.senderId
        val myEpoch = if (iAmSender) dto.senderEpoch else dto.recipientEpoch
        val partnerEpoch = if (iAmSender) dto.recipientEpoch else dto.senderEpoch
        val identity = identityKeyStore.getOrCreate(myUid)
        check(identity.epoch == myEpoch) { "local epoch ${identity.epoch}, message needs $myEpoch" }
        val peer = peerKeyRegistry.cachedPeerKey(partnerId, partnerEpoch)
            ?: peerKeyRegistry.peerKeyForEpoch(partnerId, partnerEpoch)
            ?: error("no key for $partnerId epoch $partnerEpoch")
        val key = ConversationCrypto.deriveConversationKey(
            identity.privateKey, peer.publicKey, myUid, partnerId, identity.epoch, peer.epoch,
        )
        val aad = messageAad(dto.conversationId, dto.id, dto.senderId, dto.type)
        val plaintext = ConversationCrypto.decrypt(key, unb64(dto.nonce), unb64(dto.ciphertext), aad)
        return json.decodeFromString(String(plaintext))
    }

    private fun undecryptable(dto: RemoteMessageDto, hasPendingWrites: Boolean): Message =
        dto.copy(
            text = UNDECRYPTABLE_TEXT,
            type = Message.TYPE_TEXT,
            mediaItems = emptyList(),
            replyToMessageId = null,
            replyToText = null,
            replyToSenderId = null,
        ).toDomain(hasPendingWrites)

    companion object {
        const val UNDECRYPTABLE_TEXT = "Encrypted message"
    }
}
