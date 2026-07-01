package com.example.messenger.data.remote.firebase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isNull
import com.example.messenger.domain.model.MessageStatus
import org.junit.jupiter.api.Test

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
        assertThat(message.id).isEqualTo("m1")
        assertThat(message.conversationId).isEqualTo("c1")
        assertThat(message.senderId).isEqualTo("s1")
        assertThat(message.text).isEqualTo("hello")
        assertThat(message.timestamp).isEqualTo(1234L)
        assertThat(message.status).isEqualTo(MessageStatus.SENT)
        assertThat(message.isRead).isFalse()
    }

    @Test
    fun `blank conversationId yields null`() {
        assertThat(parseIncomingMessage(base - "conversationId")).isNull()
    }

    @Test
    fun `blank messageId yields null`() {
        assertThat(parseIncomingMessage(base - "messageId")).isNull()
    }

    @Test
    fun `missing timestamp falls back to a positive value`() {
        val message = parseIncomingMessage(base - "timestamp")!!
        assertThat(message.timestamp).isGreaterThan(0L)
    }
}
