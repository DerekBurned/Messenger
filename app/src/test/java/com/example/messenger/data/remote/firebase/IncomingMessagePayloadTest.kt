package com.example.messenger.data.remote.firebase

import com.example.messenger.domain.model.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IncomingMessagePayloadTest {

    private val base = mapOf(
        "type" to "message",
        "conversationId" to "c1",
        "messageId" to "m1",
        "senderId" to "s1",
        "preview" to "hello",
        "timestamp" to "1234",
    )

    @Test
    fun `valid payload maps to a delivered incoming text message`() {
        val message = parseIncomingMessage(base)!!
        assertEquals("m1", message.id)
        assertEquals("c1", message.conversationId)
        assertEquals("s1", message.senderId)
        assertEquals("hello", message.text)
        assertEquals(1234L, message.timestamp)
        assertEquals(MessageStatus.SENT, message.status)
        assertEquals(false, message.isRead)
    }

    @Test
    fun `blank conversationId yields null`() {
        assertNull(parseIncomingMessage(base - "conversationId"))
    }

    @Test
    fun `blank messageId yields null`() {
        assertNull(parseIncomingMessage(base - "messageId"))
    }

    @Test
    fun `missing timestamp falls back to a positive value`() {
        val message = parseIncomingMessage(base - "timestamp")!!
        assert(message.timestamp > 0L)
    }
}
