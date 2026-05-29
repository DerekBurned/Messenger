package com.example.messenger.presentation.intent

import com.example.messenger.domain.model.Message
import com.example.messenger.presentation.base.UiIntent

sealed interface ChatIntent : UiIntent {
    data class TextChanged(val text: String) : ChatIntent
    data class SendMessage(val text: String) : ChatIntent
    data class MarkAsRead(val message: Message) : ChatIntent
    data class DeleteMessage(val message: Message) : ChatIntent
    data class SetReplyTo(val message: Message) : ChatIntent
    data class Forward(val message: Message) : ChatIntent
    data object ClearReply : ChatIntent
    data object ClearForward : ChatIntent
    data object ClearError : ChatIntent
}
