 package com.example.messenger.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messenger.presentation.MainScreenWithNav
import com.example.messenger.presentation.screens.*
import com.example.messenger.presentation.viewmodel.AuthViewModel

sealed class Screens(val route: String) {
    object LoginScreen : Screens("login_screen")
    object RegisterScreen : Screens("register_screen")
    object MainScreen : Screens("main_screen")
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
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val startDestination = if (authState.isAuthenticated) {
        Screens.MainScreen.route
    } else {
        Screens.LoginScreen.route
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
        composable(route = Screens.LoginScreen.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screens.RegisterScreen.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screens.MainScreen.route) {
                        popUpTo(Screens.LoginScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screens.RegisterScreen.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screens.MainScreen.route) {
                        popUpTo(Screens.LoginScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screens.MainScreen.route) {
            MainScreenWithNav(
                onChatClick = { conversationId, partnerId, partnerName ->
                    navController.navigate(
                        Screens.ChatScreen.createRoute(
                            conversationId,
                            partnerId,
                            partnerName
                        )
                    )
                },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screens.ProfileScreen.route)
                },
                onSearchClick = {
                    navController.navigate(Screens.SearchScreen.route)
                },
                onSettingsClick = {
                    navController.navigate(Screens.SettingsScreen.route)
                },
            )
        }

        composable(route = Screens.SettingsScreen.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Screens.ProfileScreen.route) },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
                },
            )
        }

        composable(route = Screens.ProfileScreen.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
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
                    authViewModel.logout()
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
                },
                onChangeAccount = { navController.navigate(Screens.ChangeAccountScreen.route) },
            )
        }

        composable(route = Screens.ChangeAccountScreen.route) {
            ChangeAccountScreen(
                onBackClick = { navController.popBackStack() },
                onAddAccount = { navController.navigate(Screens.LoginScreen.route) },
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
            CallScreen(onCallEnded = { navController.popBackStack() })
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
        ) {
            ChatScreenWithNav(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

    }
}
