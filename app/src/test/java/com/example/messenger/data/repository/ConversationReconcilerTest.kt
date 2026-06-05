package com.example.messenger.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationReconcilerTest {

    @Test
    fun `server set missing a local id marks it stale`() {
        val stale = ConversationReconciler.staleIds(
            local = setOf("a", "b", "c"),
            remote = RemoteConversations.Server(setOf("a", "c")),
        )
        assertEquals(setOf("b"), stale)
    }

    @Test
    fun `empty server set prunes all local`() {
        val stale = ConversationReconciler.staleIds(
            local = setOf("a", "b"),
            remote = RemoteConversations.Server(emptySet()),
        )
        assertEquals(setOf("a", "b"), stale)
    }

    @Test
    fun `unavailable remote never prunes`() {
        val stale = ConversationReconciler.staleIds(
            local = setOf("a", "b"),
            remote = RemoteConversations.Unavailable,
        )
        assertEquals(emptySet<String>(), stale)
    }

    @Test
    fun `server superset prunes nothing`() {
        val stale = ConversationReconciler.staleIds(
            local = setOf("a"),
            remote = RemoteConversations.Server(setOf("a", "b")),
        )
        assertEquals(emptySet<String>(), stale)
    }

    @Test
    fun `exact match prunes nothing`() {
        val stale = ConversationReconciler.staleIds(
            local = setOf("a", "b"),
            remote = RemoteConversations.Server(setOf("a", "b")),
        )
        assertEquals(emptySet<String>(), stale)
    }

    @Test
    fun `empty local is always empty`() {
        val stale = ConversationReconciler.staleIds(
            local = emptySet(),
            remote = RemoteConversations.Server(setOf("a")),
        )
        assertEquals(emptySet<String>(), stale)
    }
}
