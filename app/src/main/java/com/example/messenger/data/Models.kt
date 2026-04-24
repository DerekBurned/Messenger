package com.example.messenger.data

import java.util.Date
import java.util.UUID

// ─── Data Models ──────────────────────────────────────────────────────────────

enum class MessageStatus { SENDING, SENT, DELIVERED, READ }
enum class MessageType   { TEXT, AUDIO, VIDEO, IMAGE, FILE }
enum class ContactStatus { ONLINE, OFFLINE, DND }
enum class CallType      { INCOMING, OUTGOING, MISSED }

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: Date = Date(),
    val status: MessageStatus = MessageStatus.SENDING,
    val isOwn: Boolean,
    val replyToId: String? = null,
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val type: MessageType = MessageType.TEXT
)

data class Chat(
    val id: String,
    val contactId: String,
    val lastMessage: String = "",
    val lastMessageTime: Date? = null,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false
)

data class Contact(
    val id: String,
    val name: String,
    val username: String,
    val phone: String,
    val dob: String = "",
    val avatar: String? = null,
    val status: ContactStatus = ContactStatus.OFFLINE,
    val lastSeen: String = ""
)

data class Call(
    val id: String,
    val contactId: String,
    val type: CallType,
    val timestamp: Date,
    val duration: Int? = null   // in seconds
)

data class Account(
    val id: String,
    val name: String,
    val phone: String,
    val avatar: String? = null
)

data class UserProfile(
    val id: String = "me",
    val name: String = "John Doe",
    val username: String = "johndoe",
    val phone: String = "+1 234 567 8900",
    val dob: String = "1990-01-15",
    val avatar: String? = null
)

// ─── Mock Data ─────────────────────────────────────────────────────────────────

private fun hoursAgo(mins: Long): Date =
    Date(System.currentTimeMillis() - mins * 60_000)

val mockContacts = listOf(
    Contact("c1", "Alice Johnson",  "alice_j",  "+1 555 0101", "1992-03-15", status = ContactStatus.ONLINE),
    Contact("c2", "Bob Smith",      "bob_smith", "+1 555 0102", "1988-07-22", status = ContactStatus.OFFLINE, lastSeen = "last seen recently"),
    Contact("c3", "Carol White",    "carol_w",   "+1 555 0103", "1995-11-08", status = ContactStatus.ONLINE),
    Contact("c4", "David Brown",    "david_b",   "+1 555 0104", "1990-05-30", status = ContactStatus.OFFLINE, lastSeen = "last seen at 14:32"),
    Contact("c5", "Emma Davis",     "emma_d",    "+1 555 0105", "1997-09-14", status = ContactStatus.ONLINE),
    Contact("c6", "Frank Miller",   "frank_m",   "+1 555 0106", "1985-12-01", status = ContactStatus.DND),
    Contact("c7", "Grace Wilson",   "grace_w",   "+1 555 0107", "1993-04-18", status = ContactStatus.OFFLINE, lastSeen = "last seen yesterday"),
    Contact("c8", "Henry Moore",    "henry_m",   "+1 555 0108", "1991-08-25", status = ContactStatus.ONLINE),
)

val mockChats = listOf(
    Chat("chat1", "c1", "Hey, how are you?",   hoursAgo(2),    unreadCount = 3),
    Chat("chat2", "c2", "See you tomorrow!",    hoursAgo(45),   unreadCount = 0),
    Chat("chat3", "c3", "typing...",            hoursAgo(5),    unreadCount = 1, isTyping = true),
    Chat("chat4", "c4", "Thanks!",              hoursAgo(120),  unreadCount = 0),
    Chat("chat5", "c5", "Can we meet?",         hoursAgo(200),  unreadCount = 7),
    Chat("chat6", "c6", "Ok",                   hoursAgo(300),  unreadCount = 0),
    Chat("chat7", "c7", "Sure!",                hoursAgo(1440), unreadCount = 2),
    Chat("chat8", "c8", "Good morning!",        hoursAgo(1500), unreadCount = 0),
)

val mockMessages: Map<String, List<Message>> = mapOf(
    "chat1" to listOf(
        Message(id="m1", chatId="chat1", senderId="c1", text="Hi there!", timestamp=hoursAgo(60), status=MessageStatus.READ, isOwn=false),
        Message(id="m2", chatId="chat1", senderId="me", text="Hello! How are you?", timestamp=hoursAgo(58), status=MessageStatus.READ, isOwn=true),
        Message(id="m3", chatId="chat1", senderId="c1", text="I'm doing great, thanks! What about you?", timestamp=hoursAgo(55), status=MessageStatus.READ, isOwn=false),
        Message(id="m4", chatId="chat1", senderId="me", text="Pretty good! Working on a new project.", timestamp=hoursAgo(50), status=MessageStatus.READ, isOwn=true),
        Message(id="m5", chatId="chat1", senderId="c1", text="Oh nice! What kind of project?", timestamp=hoursAgo(48), status=MessageStatus.READ, isOwn=false),
        Message(id="m6", chatId="chat1", senderId="me", text="A messenger app actually 😄", timestamp=hoursAgo(45), status=MessageStatus.DELIVERED, isOwn=true),
        Message(id="m7", chatId="chat1", senderId="c1", text="That's so cool! Can't wait to see it.", timestamp=hoursAgo(3), status=MessageStatus.READ, isOwn=false),
        Message(id="m8", chatId="chat1", senderId="c1", text="Hey, how are you?", timestamp=hoursAgo(2), status=MessageStatus.READ, isOwn=false),
    ),
    "chat2" to listOf(
        Message(id="m1", chatId="chat2", senderId="me", text="Are we still on for tomorrow?", timestamp=hoursAgo(90), status=MessageStatus.READ, isOwn=true),
        Message(id="m2", chatId="chat2", senderId="c2", text="Yes of course!", timestamp=hoursAgo(85), status=MessageStatus.READ, isOwn=false),
        Message(id="m3", chatId="chat2", senderId="c2", text="See you tomorrow!", timestamp=hoursAgo(45), status=MessageStatus.READ, isOwn=false),
    ),
    "chat3" to listOf(
        Message(id="m1", chatId="chat3", senderId="c3", text="Hey!", timestamp=hoursAgo(20), status=MessageStatus.READ, isOwn=false),
        Message(id="m2", chatId="chat3", senderId="me", text="Hi Carol!", timestamp=hoursAgo(18), status=MessageStatus.READ, isOwn=true),
        Message(id="m3", chatId="chat3", senderId="c3", text="How's the new project going?", timestamp=hoursAgo(5), status=MessageStatus.READ, isOwn=false),
    ),
)

val mockCalls = listOf(
    Call("call1", "c1", CallType.INCOMING, hoursAgo(30),   245),
    Call("call2", "c3", CallType.OUTGOING, hoursAgo(90),   120),
    Call("call3", "c2", CallType.MISSED,   hoursAgo(180)),
    Call("call4", "c5", CallType.MISSED,   hoursAgo(360)),
    Call("call5", "c4", CallType.OUTGOING, hoursAgo(720),  60),
    Call("call6", "c1", CallType.INCOMING, hoursAgo(1440), 330),
    Call("call7", "c7", CallType.MISSED,   hoursAgo(2880)),
    Call("call8", "c8", CallType.OUTGOING, hoursAgo(4320), 180),
)

val mockAccounts = listOf(
    Account("acc1", "John Doe",  "+1 234 567 8900"),
    Account("acc2", "John Work", "+1 234 567 8901"),
)
