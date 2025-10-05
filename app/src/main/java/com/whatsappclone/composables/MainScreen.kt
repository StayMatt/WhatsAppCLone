package com.whatsappclone.composables

import androidx.compose.runtime.*
import androidx.compose.material3.*

sealed class Screen {
    object Welcome : Screen()
    object RegisterPhone : Screen()
    object VerifyCode : Screen()
    object EnterName : Screen()
    object Chats : Screen()
    data class Chat(val chatId: String) : Screen()
    object AddContact : Screen()
    object Profile : Screen()
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("123456") } // código simulado
    var userName by remember { mutableStateOf("") }

    when (val screen = currentScreen) {
        is Screen.Welcome -> WelcomeScreen(
            onAccept = { currentScreen = Screen.RegisterPhone }
        )

        is Screen.RegisterPhone -> RegisterPhoneScreen(
            onPhoneEntered = { number ->
                phoneNumber = number
                currentScreen = Screen.VerifyCode
            },
            onBack = { currentScreen = Screen.Welcome }
        )

        is Screen.VerifyCode -> VerifyCodeScreen(
            correctCode = verificationCode,
            onVerified = { currentScreen = Screen.EnterName },
            onBack = { currentScreen = Screen.RegisterPhone }
        )

        is Screen.EnterName -> EnterNameScreen(
            onNameEntered = { name ->
                userName = name
                currentScreen = Screen.Chats
            },
            onBack = { currentScreen = Screen.VerifyCode }
        )

        is Screen.Chats -> ChatsScreen(
            onOpenChat = { /* ejemplo */ },
            onAddContact = { currentScreen = Screen.AddContact },
            onOpenProfile = { currentScreen = Screen.Profile }
        )

        is Screen.AddContact -> AddContactScreen(onBack = { currentScreen = Screen.Chats })
        is Screen.Profile -> ProfileScreen(onBack = { currentScreen = Screen.Chats })
        is Screen.Chat -> ChatScreen(chatId = screen.chatId, onBack = { currentScreen = Screen.Chats })
    }
}