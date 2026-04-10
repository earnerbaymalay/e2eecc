package com.cypherchat

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cypherchat.ui.screen.ChatListScreen
import com.cypherchat.ui.screen.ConversationScreen
import com.cypherchat.ui.screen.OnboardingScreen

private object Routes {
    const val ONBOARDING = "onboarding"
    const val CHAT_LIST = "chats"
    const val CONVERSATION = "conversation/{conversationId}"

    fun conversation(id: String) = "conversation/$id"
}

@Composable
fun CypherchatNavigation(startAtChatList: Boolean = false) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = if (startAtChatList) Routes.CHAT_LIST else Routes.ONBOARDING
    ) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onComplete = {
                (nav.context as? MainActivity)?.markOnboardingComplete()
                nav.navigate(Routes.CHAT_LIST) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.CHAT_LIST) {
            ChatListScreen(onOpenChat = { conversationId ->
                nav.navigate(Routes.conversation(conversationId))
            })
        }

        composable(
            route = Routes.CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
                ?: return@composable
            ConversationScreen(
                conversationId = conversationId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
