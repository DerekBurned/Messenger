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
import com.example.messenger.presentation.screens.*
import com.example.messenger.presentation.viewmodel.AuthViewModel

sealed class Screens(val route: String) {
    object LoginScreen : Screens("login_screen")
    object RegisterScreen : Screens("register_screen")
    object MainScreen : Screens("main_screen")
    object ChatScreen : Screens("chat_screen/{conversationId}") {
        fun createRoute(conversationId: String) = "chat_screen/$conversationId"
    }
    object ProfileScreen : Screens("profile_screen")
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
                onChatClick = { conversationId ->
                    navController.navigate(Screens.ChatScreen.createRoute(conversationId))
                },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screens.ProfileScreen.route)
                }
            )
        }

        composable(
            route = Screens.ChatScreen.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            ChatScreenWithNav(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screens.ProfileScreen.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
