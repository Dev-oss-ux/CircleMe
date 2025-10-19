package com.barry.circleme.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.barry.circleme.ui.auth.AuthScreen
import com.barry.circleme.ui.chat.ChatScreen
import com.barry.circleme.ui.conversations.ConversationsViewModel
import com.barry.circleme.ui.create_post.CreatePostScreen
import com.barry.circleme.ui.find_user.FindUserScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val AUTH_SCREEN = "auth_screen"
    const val MAIN_SCREEN = "main_screen"
    const val CREATE_POST_SCREEN = "create_post_screen"
    const val CHAT_SCREEN = "chat_screen"
    const val FIND_USER_SCREEN = "find_user_screen"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberAnimatedNavController()
    val auth = Firebase.auth

    val startDestination = if (auth.currentUser != null) Routes.MAIN_SCREEN else Routes.AUTH_SCREEN

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Routes.AUTH_SCREEN) {
            AuthScreen(onSignInSuccess = {
                navController.navigate(Routes.MAIN_SCREEN) {
                    popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                }
            })
        }
        composable(Routes.MAIN_SCREEN) {
            MainScreen(appNavController = navController)
        }
        composable(Routes.CREATE_POST_SCREEN) {
            CreatePostScreen(onPostCreated = { navController.popBackStack() })
        }
        composable(
            route = "${Routes.CHAT_SCREEN}/{recipientId}/{recipientName}",
            arguments = listOf(
                navArgument("recipientId") { type = NavType.StringType },
                navArgument("recipientName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipientName = backStackEntry.arguments?.getString("recipientName") ?: ""
            ChatScreen(recipientName = recipientName)
        }
        composable(Routes.FIND_USER_SCREEN) {
            val conversationsViewModel: ConversationsViewModel = viewModel()
            FindUserScreen(onUserClick = { user ->
                conversationsViewModel.startConversation(user) {
                    val encodedName = URLEncoder.encode(user.displayName ?: "User", StandardCharsets.UTF_8.toString())
                    navController.navigate("${Routes.CHAT_SCREEN}/${user.uid}/$encodedName")
                }
            })
        }
    }
}
