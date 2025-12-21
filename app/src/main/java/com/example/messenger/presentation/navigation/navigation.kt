package com.example.messenger.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.messenger.presentation.screens.*

sealed class Screens(val route: String) {
    object LoginScreen : Screens("login_screen")
    object RegisterScreen : Screens("register_screen")
    object MainScreen : Screens("main_screen")
    object ChatScreen : Screens("chat_screen")
    object ProfileScreen : Screens("profile_screen")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screens.LoginScreen.route,
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
        // 1. Экран входа
        composable(route = Screens.LoginScreen.route) {
            LoginScreen(
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

        // 2. Экран регистрации
        composable(route = Screens.RegisterScreen.route) {
            RegisterScreen(
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

        // 3. Главный экран (список чатов)
        composable(route = Screens.MainScreen.route) {
            MainScreenWithNav(
                onChatClick = {
                    navController.navigate(Screens.ChatScreen.route)
                },
                onLogoutClick = {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.MainScreen.route) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(Screens.ProfileScreen.route)
                }
            )
        }

        // 4. Экран чата
        composable(route = Screens.ChatScreen.route) {
            ChatScreenWithNav(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Экран профиля
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