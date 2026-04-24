package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.messenger.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class ActiveCall(val contact: Contact, val callType: String) // "incoming" | "outgoing"

data class AppState(
    val isLoggedIn: Boolean = false,
    val user: UserProfile = UserProfile(),
    val contacts: List<Contact> = mockContacts,
    val chats: List<Chat> = mockChats,
    val calls: List<Call> = mockCalls,
    val messages: Map<String, List<Message>> = mockMessages,
    val accounts: List<Account> = mockAccounts,
    val currentAccountId: String = "acc1",
    val darkMode: Boolean = false,
    val activeCall: ActiveCall? = null,
    val incomingCall: Contact? = null
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    // ── Auth ───────────────────────────────────────────────────────────────────

    fun login(phoneOrEmail: String, password: String) {
        _state.update { it.copy(isLoggedIn = true) }
    }

    fun register(name: String, phoneOrEmail: String, password: String) {
        _state.update {
            it.copy(
                isLoggedIn = true,
                user = it.user.copy(name = name)
            )
        }
    }

    fun logout() {
        _state.update { it.copy(isLoggedIn = false) }
    }

    // ── Profile ────────────────────────────────────────────────────────────────

    fun updateProfile(name: String? = null, phone: String? = null,
                      username: String? = null, dob: String? = null) {
        _state.update { s ->
            s.copy(user = s.user.copy(
                name     = name     ?: s.user.name,
                phone    = phone    ?: s.user.phone,
                username = username ?: s.user.username,
                dob      = dob      ?: s.user.dob
            ))
        }
    }

    // ── Messages ───────────────────────────────────────────────────────────────

    fun sendMessage(chatId: String, text: String, replyToId: String? = null) {
        val newMsg = Message(
            id        = "msg_${UUID.randomUUID()}",
            chatId    = chatId,
            senderId  = "me",
            text      = text,
            timestamp = Date(),
            status    = MessageStatus.SENDING,
            isOwn     = true,
            replyToId = replyToId
        )
        _state.update { s ->
            val updatedMessages = s.messages.toMutableMap()
            updatedMessages[chatId] = (updatedMessages[chatId] ?: emptyList()) + newMsg
            val updatedChats = s.chats.map { c ->
                if (c.id == chatId) c.copy(lastMessage = text, lastMessageTime = Date(), isTyping = false) else c
            }
            s.copy(messages = updatedMessages, chats = updatedChats)
        }
        // Simulate delivered → read status
        updateMessageStatus(chatId, newMsg.id, MessageStatus.DELIVERED, delayMs = 1000)
        updateMessageStatus(chatId, newMsg.id, MessageStatus.READ, delayMs = 2500)
    }

    private fun updateMessageStatus(chatId: String, msgId: String,
                                    status: MessageStatus, delayMs: Long) {
        // In real project, use viewModelScope + delay
        // For demo: use a Handler or coroutine
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _state.update { s ->
                val updatedMessages = s.messages.toMutableMap()
                updatedMessages[chatId] = (updatedMessages[chatId] ?: emptyList()).map { m ->
                    if (m.id == msgId) m.copy(status = status) else m
                }
                s.copy(messages = updatedMessages)
            }
        }, delayMs)
    }

    fun deleteMessage(chatId: String, messageId: String) {
        _state.update { s ->
            val updatedMessages = s.messages.toMutableMap()
            updatedMessages[chatId] = (updatedMessages[chatId] ?: emptyList())
                .filter { it.id != messageId }
            s.copy(messages = updatedMessages)
        }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        _state.update { s ->
            val updatedMessages = s.messages.toMutableMap()
            updatedMessages[chatId] = (updatedMessages[chatId] ?: emptyList()).map { m ->
                if (m.id == messageId) m.copy(text = newText, isEdited = true) else m
            }
            s.copy(messages = updatedMessages)
        }
    }

    // ── Chats ──────────────────────────────────────────────────────────────────

    fun markChatRead(chatId: String) {
        _state.update { s ->
            s.copy(chats = s.chats.map { c ->
                if (c.id == chatId) c.copy(unreadCount = 0) else c
            })
        }
    }

    fun markAllRead() {
        _state.update { s ->
            s.copy(chats = s.chats.map { it.copy(unreadCount = 0) })
        }
    }

    fun deleteChats(chatIds: List<String>) {
        _state.update { s ->
            s.copy(chats = s.chats.filter { it.id !in chatIds })
        }
    }

    // ── Contacts ───────────────────────────────────────────────────────────────

    fun deleteContact(contactId: String) {
        _state.update { s ->
            s.copy(
                contacts = s.contacts.filter { it.id != contactId },
                chats    = s.chats.filter { it.contactId != contactId }
            )
        }
    }

    fun updateContact(contactId: String, name: String, phone: String) {
        _state.update { s ->
            s.copy(contacts = s.contacts.map { c ->
                if (c.id == contactId) c.copy(name = name, phone = phone) else c
            })
        }
    }

    fun updateContactName(contactId: String, newName: String) {
        _state.update { s ->
            s.copy(contacts = s.contacts.map { c ->
                if (c.id == contactId) c.copy(name = newName) else c
            })
        }
    }

    // ── Calls ──────────────────────────────────────────────────────────────────

    fun startCall(contactId: String) {
        val contact = _state.value.contacts.find { it.id == contactId } ?: return
        _state.update { it.copy(activeCall = ActiveCall(contact, "outgoing")) }
    }

    fun endCall() {
        _state.update { it.copy(activeCall = null) }
    }

    fun acceptCall() {
        val incoming = _state.value.incomingCall ?: return
        _state.update { it.copy(
            activeCall   = ActiveCall(incoming, "incoming"),
            incomingCall = null
        )}
    }

    fun declineCall() {
        _state.update { it.copy(incomingCall = null) }
    }

    fun simulateIncomingCall() {
        val contact = _state.value.contacts.random()
        _state.update { it.copy(incomingCall = contact) }
    }

    // ── Settings ───────────────────────────────────────────────────────────────

    fun toggleDarkMode() {
        _state.update { it.copy(darkMode = !it.darkMode) }
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    fun switchAccount(accountId: String) {
        val account = _state.value.accounts.find { it.id == accountId } ?: return
        _state.update { s ->
            s.copy(
                user             = s.user.copy(name = account.name, phone = account.phone),
                currentAccountId = accountId
            )
        }
    }

    fun addAccount() {
        val newId  = "acc_${UUID.randomUUID()}"
        val newAcc = Account(id = newId, name = "New Account", phone = "+1 000 000 0000")
        _state.update { s -> s.copy(accounts = s.accounts + newAcc) }
    }
}
