package com.example.messenger.data.remote.dto

import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageDtoTest {

    @Test
    fun `pending local write is reported as SENDING regardless of stored status`() {
        val dto = RemoteMessageDto(
            id = "m5",
            conversationId = "c1",
            senderId = "me",
            status = "SENT",
            type = Message.TYPE_TEXT,
            text = "hi",
        )
        assertEquals(MessageStatus.SENDING, dto.toDomain(hasPendingWrites = true).status)
    }

    @Test
    fun `server-acked write keeps its stored status`() {
        val dto = RemoteMessageDto(
            id = "m6",
            conversationId = "c1",
            senderId = "me",
            status = "SENT",
            type = Message.TYPE_TEXT,
            text = "hi",
        )
        assertEquals(MessageStatus.SENT, dto.toDomain(hasPendingWrites = false).status)
    }

    @Test
    fun `server-acked read message is not downgraded to sending`() {
        val dto = RemoteMessageDto(
            id = "m7",
            conversationId = "c1",
            senderId = "me",
            status = "READ",
            type = Message.TYPE_TEXT,
            text = "hi",
        )
        assertEquals(MessageStatus.READ, dto.toDomain(hasPendingWrites = false).status)
    }

    @Test
    fun `read status maps to isRead true even when isRead field is false`() {
        val dto = RemoteMessageDto(
            id = "m1",
            conversationId = "c1",
            senderId = "other",
            status = "READ",
            isRead = false,
            type = Message.TYPE_TEXT,
            text = "hi",
        )
        assertTrue(dto.toDomain().isRead)
    }

    @Test
    fun `sent status keeps isRead false`() {
        val dto = RemoteMessageDto(
            id = "m2",
            conversationId = "c1",
            senderId = "other",
            status = "SENT",
            isRead = false,
            type = Message.TYPE_TEXT,
            text = "hi",
        )
        assertFalse(dto.toDomain().isRead)
    }

    @Test
    fun `explicit isRead true is preserved when status is sent`() {
        val dto = RemoteMessageDto(
            id = "m3",
            conversationId = "c1",
            senderId = "other",
            status = "SENT",
            isRead = true,
            type = Message.TYPE_TEXT,
            text = "hi",
        )
        assertTrue(dto.toDomain().isRead)
    }

    @Test
    fun `read status maps to isRead true for call messages`() {
        val dto = RemoteMessageDto(
            id = "m4",
            conversationId = "c1",
            senderId = "other",
            status = "READ",
            isRead = false,
            type = Message.TYPE_MISSED_CALL,
        )
        assertTrue(dto.toDomain().isRead)
    }
}
