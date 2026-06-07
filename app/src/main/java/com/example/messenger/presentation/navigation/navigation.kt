 package com.example.messenger.presentation.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.data.remote.call.CallForegroundService
import com.example.messenger.presentation.components.ActiveCallBar
import com.example.messenger.presentation.components.FloatingTabBar
import com.example.messenger.presentation.components.IncomingCallBar
import com.example.messenger.presentation.components.MainTab
import com.example.messenger.presentation.intent.AuthIntent
import com.example.messenger.presentation.screens.*
import com.example.messenger.presentation.viewmodel.AuthViewModel

sealed class Screens(val route: String) {
    object AuthScreen : Screens("auth_screen")
    object ChatsScreen : Screens("chats_screen")
    object CallsScreen : Screens("calls_screen")
    object ContactsScreen : Screens("contacts_screen")
    object SearchUsersScreen : Screens("search_users_screen")
    object ChatScreen : Screens("chat_screen/{conversationId}/{partnerId}/{partnerName}") {
        fun createRoute(conversationId: String, partnerId: String, partnerName: String): String {
            val encodedName = java.net.URLEncoder.encode(partnerName, "UTF-8")
            return "chat_screen/$conversationId/$partnerId/$encodedName"
        }
    }
    object ProfileScreen : Screens("profile_screen")
    object SearchScreen : Screens("search_screen")
    object SettingsScreen : Screens("settings_screen")
    object EditProfileScreen : Screens("edit_profile_screen")
    object ChangeAccountScreen : Screens("change_account_screen")
    object EditChatScreen : Screens("edit_chat_screen")
    object ChangePhoneScreen : Screens("change_phone_screen")
    object CallScreen : Screens("call_screen/{partnerId}/{partnerName}/{partnerPhone}") {
        fun createRoute(partnerId: String, partnerName: String, partnerPhone: String): String {
            val encodedName = java.net.URLEncoder.encode(partnerName, "UTF-8")
            val encodedPhone = java.net.URLEncoder.encode(partnerPhone, "UTF-8")
            return "call_screen/$partnerId/$encodedName/$encodedPhone"
        }
    }
    object ChatUserProfileScreen : Screens("chat_user_profile_screen/{userId}") {
        fun createRoute(userId: String): String = "chat_user_profile_screen/$userId"
    }
    object EditContactDataScreen : Screens("edit_contact_data_screen/{contactId}") {
        fun createRoute(contactId: String): String = "edit_contact_data_screen/$contactId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    val startDestination = if (authState.isAuthenticated) {
        Screens.ChatsScreen.route
    } else {
        Screens.AuthScreen.route
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route.orEmpty()
    val showCallBar = !currentRoute.startsWith("call_screen") &&
        currentRoute != Screens.AuthScreen.route
    val tabRoutes = setOf(
        Screens.ChatsScreen.route,
        Screens.CallsScreen.route,
        Screens.ContactsScreen.route,
        Screens.SettingsScreen.route
    )
    val showTabBar = currentRoute in tabRoutes
    val selectedTab = when (currentRoute) {
        Screens.CallsScreen.route -> MainTab.CALLS
        Screens.ContactsScreen.route -> MainTab.CONTACTS
        Screens.SettingsScreen.route -> MainTab.SETTINGS
        else -> MainTab.CHATS
    }
    val context = LocalContext.current

    fun logout() {
        authViewModel.dispatch(AuthIntent.Logout)
        navController.navigate(Screens.AuthScreen.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun openActiveCall(accept: Boolean) {
        val active = ActiveCallHolder.snapshot() ?: return
        if (accept) sendCallAction(context, CallForegroundService.ACTION_ACCEPT)
        navController.navigate(
            Screens.CallScreen.createRoute(
                partnerId = active.callerId.ifBlank { "active" },
                partnerName = active.partnerName.ifBlank { "Call" },
                partnerPhone = active.partnerPhone.ifBlank { "_" },
            ),
        ) {
            launchSingleTop = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
        if (showCallBar) {
            ActiveCallBar(onClick = { openActiveCall(accept = false) })
        }
        NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(route = Screens.AuthScreen.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screens.ChatsScreen.route) {
                        popUpTo(Screens.AuthScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screens.ChatsScreen.route) {
            ChatsScreen(
                onChatClick = { conversationId, partnerId, partnerName ->
                    navController.navigate(
                        Screens.ChatScreen.createRoute(conversationId, partnerId, partnerName)
                    )
                },
                onLogoutClick = { logout() },
                onSearchClick = { navController.navigate(Screens.SearchScreen.route) },
            )
        }

        composable(route = Screens.CallsScreen.route) {
            CallsScreen(onLogoutClick = { logout() })
        }

        composable(route = Screens.ContactsScreen.route) {
            ContactsScreen(
                onLogoutClick = { logout() },
                onContactClick = { userId ->
                    navController.navigate(Screens.ChatUserProfileScreen.createRoute(userId))
                },
            )
        }

        composable(route = Screens.SettingsScreen.route) {
            SettingsScreen(
                onProfileClick = { navController.navigate(Screens.ProfileScreen.route) },
                onLogoutClick = {
                    authViewModel.dispatch(AuthIntent.Logout)
                    navController.navigate(Screens.AuthScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }

        composable(route = Screens.ProfileScreen.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate(Screens.AuthScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onStartEditing = {
                    navController.navigate(Screens.EditProfileScreen.route)
                },
            )
        }

        composable(route = Screens.SearchScreen.route) {
            SearchUsersScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onConversationCreated = { conversationId, partnerId, partnerName ->
                    navController.navigate(Screens.ChatScreen.createRoute(conversationId, partnerId, partnerName)) {
                        popUpTo(Screens.SearchScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screens.EditProfileScreen.route) {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onLogout = {
                    authViewModel.dispatch(AuthIntent.Logout)
                    navController.navigate(Screens.AuthScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onChangeAccount = { navController.navigate(Screens.ChangeAccountScreen.route) },
                onChangePhone = { navController.navigate(Screens.ChangePhoneScreen.route) },
            )
        }

        composable(route = Screens.ChangePhoneScreen.route) {
            ChangePhoneScreen(
                onBackClick = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }

        composable(route = Screens.ChangeAccountScreen.route) {
            ChangeAccountScreen(
                onBackClick = { navController.popBackStack() },
                onAddAccount = { navController.navigate(Screens.AuthScreen.route) },
            )
        }

        composable(route = Screens.EditChatScreen.route) {
            EditChatScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screens.CallScreen.route,
            arguments = listOf(
                navArgument("partnerId") { type = NavType.StringType },
                navArgument("partnerName") { type = NavType.StringType },
                navArgument("partnerPhone") { type = NavType.StringType },
            )
        ) {
            CallScreen(
                onCallEnded = { navController.popBackStack() },
                onOpenChat = { conversationId, partnerId, partnerName ->
                    navController.navigate(
                        Screens.ChatScreen.createRoute(conversationId, partnerId, partnerName),
                    ) {
                        popUpTo(Screens.CallScreen.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Screens.ChatUserProfileScreen.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            ChatUserProfileScreen(
                onBackClick = { navController.popBackStack() },
                onCallClick = { partnerId, partnerName, partnerPhone ->
                navController.navigate(Screens.CallScreen.createRoute(partnerId, partnerName, partnerPhone))
            },
            onEditClick = {
                    navController.navigate(Screens.EditContactDataScreen.createRoute(userId))
                },
            )
        }

        composable(
            route = Screens.EditContactDataScreen.route,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) {
            EditContactDataScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screens.ChatScreen.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("partnerId") { type = NavType.StringType; defaultValue = "" },
                navArgument("partnerName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId").orEmpty()
            val partnerName = backStackEntry.arguments?.getString("partnerName").orEmpty()
            ChatScreenWithNav(
                onBackClick = {
                    navController.popBackStack()
                },
                onIntercultorProfileClick = {
                    if (partnerId.isNotBlank()) {
                        navController.navigate(
                            Screens.ChatUserProfileScreen.createRoute(partnerId)
                        )
                    }
                },
                onCallClick = {
                    navController.navigate(Screens.CallScreen.createRoute(partnerId,
                        partnerName, ""))
                }
            )
        }

        }
        }
        if (showTabBar) {
            FloatingTabBar(
                selected = selectedTab,
                onSelect = { tab -> navController.navigateToTab(tab.route()) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        if (showCallBar) {
            IncomingCallBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp),
                onAccept = { openActiveCall(accept = true) },
                onDecline = { sendCallAction(context, CallForegroundService.ACTION_DECLINE) },
                onOpen = { openActiveCall(accept = false) },
            )
        }
    }
}

private fun sendCallAction(context: Context, action: String) {
    val intent = Intent(context, CallForegroundService::class.java).setAction(action)
    runCatching { context.startService(intent) }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun MainTab.route(): String = when (this) {
    MainTab.CHATS -> Screens.ChatsScreen.route
    MainTab.CALLS -> Screens.CallsScreen.route
    MainTab.CONTACTS -> Screens.ContactsScreen.route
    MainTab.SETTINGS -> Screens.SettingsScreen.route
}
