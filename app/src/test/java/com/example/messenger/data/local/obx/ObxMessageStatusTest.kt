package com.example.messenger.data.local.obx

import com.example.messenger.domain.model.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ObxMessageStatusTest {

    private fun message(status: String) = ObxMessage(
        uid = "m1",
        conversationId = "c1",
        senderId = "me",
        text = "hi",
        status = status,
    )

    @Test
    fun `sending status is preserved when read back from local store`() {
        assertEquals(MessageStatus.SENDING, message("SENDING").toDomain().status)
    }

    @Test
    fun `read status is preserved`() {
        assertEquals(MessageStatus.READ, message("READ").toDomain().status)
    }

    @Test
    fun `failed status is preserved`() {
        assertEquals(MessageStatus.FAILED, message("FAILED").toDomain().status)
    }

    @Test
    fun `sent status stays sent`() {
        assertEquals(MessageStatus.SENT, message("SENT").toDomain().status)
    }

    @Test
    fun `legacy delivered maps to sent`() {
        assertEquals(MessageStatus.SENT, message("DELIVERED").toDomain().status)
    }
}
