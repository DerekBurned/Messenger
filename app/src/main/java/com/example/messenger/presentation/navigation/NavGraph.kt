package com.example.messenger.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.messenger.presentation.screens.*
import com.example.messenger.presentation.viewmodel.MainViewModel

// ─── Route constants ──────────────────────────────────────────────────────────
object Routes {
    const val LOGIN              = "login"
    const val REGISTER           = "register"
    const val CHATS              = "chats"
    const val CHAT_INSIDE        = "chat/{chatId}"
    const val CHAT_USER_PROFILE  = "chat/{chatId}/profile"
    const val EDIT_CONTACT_DATA  = "chat/{chatId}/profile/edit"
    const val EDIT_CHATS         = "chats/edit"
    const val CALLS              = "calls"
    const val CONTACTS           = "contacts"
    const val SETTINGS           = "settings"
    const val PROFILE            = "profile"
    const val EDIT_PROFILE       = "profile/edit"
    const val CHANGE_ACCOUNT     = "change-account"

    fun chatInside(chatId: String)       = "chat/$chatId"
    fun chatUserProfile(chatId: String)  = "chat/$chatId/profile"
    fun editContactData(chatId: String)  = "chat/$chatId/profile/edit"
}

// ─── Main NavGraph ─────────────────────────────────────────────────────────────

@Composable
fun MessengerNavGraph(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    startDestination: String = Routes.LOGIN
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    val totalUnread = remember(state.chats) { state.chats.sumOf { it.unreadCount } }

    // ── Active / Incoming call overlay ─────────────────────────────────────────
    if (state.activeCall != null || state.incomingCall != null) {
        CallScreen(
            activeCall    = state.activeCall,
            incomingCall  = state.incomingCall,
            onEndCall     = viewModel::endCall,
            onAcceptCall  = viewModel::acceptCall,
            onDeclineCall = viewModel::declineCall
        )
        return
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {

        // ── Auth ───────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess      = {
                    viewModel.login("", "") // Исправлено: LoginScreen обычно сам управляет вводом
                    navController.navigate(Routes.CHATS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { name, phone, pass ->
                    viewModel.register(name, phone, pass)
                    navController.navigate(Routes.CHATS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Chats ──────────────────────────────────────────────────────────────
        composable(Routes.CHATS) {
            ChatsScreen(
                chats         = state.chats,
                contacts      = state.contacts,
                totalUnread   = totalUnread,
                onChatClick   = { chatId ->
                    viewModel.markChatRead(chatId)
                    navController.navigate(Routes.chatInside(chatId))
                },
                onEditClick   = { navController.navigate(Routes.EDIT_CHATS) },
                navController = navController
            )
        }

        composable(
            route     = Routes.CHAT_INSIDE,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId  = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val chat    = state.chats.find { it.id == chatId }
            val contact = chat?.let { c -> state.contacts.find { it.id == c.contactId } }

            ChatInsideScreen(
                chat            = chat,
                contact         = contact,
                messages        = state.messages[chatId] ?: emptyList(),
                onBackClick     = { navController.popBackStack() },
                onAvatarClick   = { navController.navigate(Routes.chatUserProfile(chatId)) },
                onSendMessage   = { text, replyToId -> viewModel.sendMessage(chatId, text, replyToId) },
                onDeleteMessage = { msgId -> viewModel.deleteMessage(chatId, msgId) },
                onEditMessage   = { msgId, newText -> viewModel.editMessage(chatId, msgId, newText) }
            )
        }

        // Профиль контакта из чата
        composable(
            route     = Routes.CHAT_USER_PROFILE,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId  = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val chat    = state.chats.find { it.id == chatId }
            val contact = chat?.let { c -> state.contacts.find { it.id == c.contactId } }
            if (contact != null) {
                ChatUserProfileScreen(
                    contact     = contact,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Routes.editContactData(chatId)) },
                    onCallClick = { viewModel.startCall(contact.id) }
                )
            }
        }

        // Редактирование данных контакта
        composable(
            route     = Routes.EDIT_CONTACT_DATA,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId  = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val chat    = state.chats.find { it.id == chatId }
            val contact = chat?.let { c -> state.contacts.find { it.id == c.contactId } }
            if (contact != null) {
                EditContactDataScreen(
                    contact         = contact,
                    onBackClick     = { navController.popBackStack() },
                    onSave          = { newName ->
                        viewModel.updateContactName(contact.id, newName)
                        navController.popBackStack()
                    },
                    onDeleteContact = {
                        viewModel.deleteContact(contact.id)
                        navController.navigate(Routes.CHATS) { popUpTo(Routes.CHATS) { inclusive = true } }
                    }
                )
            }
        }

        // Редактирование списка чатов
        composable(Routes.EDIT_CHATS) {
            EditChatsScreen(
                chats         = state.chats,
                contacts      = state.contacts,
                onBackClick   = { navController.popBackStack() },
                onDeleteChats = { ids -> viewModel.deleteChats(ids) },
                onMarkAllRead = { viewModel.markAllRead() }
            )
        }

        // ── Calls ──────────────────────────────────────────────────────────────
        composable(Routes.CALLS) {
            CallsScreen(
                calls         = state.calls,
                contacts      = state.contacts,
                totalUnread   = totalUnread,
                onCallContact = { contactId -> viewModel.startCall(contactId) },
                navController = navController
            )
        }

        // ── Contacts ───────────────────────────────────────────────────────────
        composable(Routes.CONTACTS) {
            ContactsScreen(
                contacts      = state.contacts,
                totalUnread   = totalUnread,
                onContactClick = { contactId ->
                    val chat = state.chats.find { it.contactId == contactId }
                    if (chat != null) {
                        viewModel.markChatRead(chat.id)
                        navController.navigate(Routes.chatInside(chat.id))
                    }
                },
                navController = navController
            )
        }

        // ── Settings ───────────────────────────────────────────────────────────
        composable(Routes.SETTINGS) {
            SettingsScreen(
                user                      = state.user,
                darkMode                  = state.darkMode,
                totalUnread               = totalUnread,
                onToggleDarkMode          = viewModel::toggleDarkMode,
                onNavigateToProfile       = { navController.navigate(Routes.PROFILE) },
                onNavigateToChangeAccount = { navController.navigate(Routes.CHANGE_ACCOUNT) },
                navController             = navController
            )
        }

        // ── Profile ────────────────────────────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                user        = state.user,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Routes.EDIT_PROFILE) }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                user        = state.user,
                onSave      = { name, phone, username, dob ->
                    viewModel.updateProfile(name, phone, username, dob)
                    navController.popBackStack()
                },
                onLogout    = {
                    viewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Смена аккаунта
        composable(Routes.CHANGE_ACCOUNT) {
            ChangeAccountScreen(
                accounts         = state.accounts,
                currentAccountId = state.currentAccountId,
                onBackClick      = { navController.popBackStack() },
                onSelectAccount  = { accountId ->
                    viewModel.switchAccount(accountId)
                    navController.navigate(Routes.CHATS) { popUpTo(0) { inclusive = true } }
                },
                onAddAccount     = { viewModel.addAccount() }
            )
        }
    }
}
