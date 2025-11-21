package com.whatsappclone.composables

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Navegación entre pantallas de la app
sealed class Screen {
    object Welcome : Screen()
    object RegisterPhone : Screen()
    object VerifyCode : Screen()
    object EnterName : Screen()
    object Chats : Screen()
    object Contacts : Screen()
    object AddContact : Screen()
    object Profile : Screen()
    object CreateGroup : Screen()

    // Pantalla del chat individual o grupal
    data class Chat(
        val chatId: String,
        val chatName: String,
        val chatType: String,
        val participants: List<String>
    ) : Screen()
}

// Guarda el estado de login usando SharedPreferences
fun saveLoginState(context: Context, isLogged: Boolean) {
    val preferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    preferences.edit().putBoolean("ESTA_LOGUEADO", isLogged).apply()
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Obtener estado guardado de login
    val preferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val estaLogueado = preferences.getBoolean("ESTA_LOGUEADO", false)

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    // Pantalla inicial según si está logueado o no
    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (estaLogueado && user != null) Screen.Chats else Screen.Welcome
        )
    }

    // Si hay error de sesión guardada sin usuario real → forzar logout
    if (estaLogueado && user == null) {
        saveLoginState(context, false)
        currentScreen = Screen.Welcome
    }

    // Variables para teléfono y verificación
    var phoneNumber by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    // Navegación principal según pantalla actual
    when (val screen = currentScreen) {

        // Pantalla inicial
        is Screen.Welcome -> WelcomeScreen(
            onAccept = { currentScreen = Screen.RegisterPhone }
        )

        // Pantalla para ingresar teléfono
        is Screen.RegisterPhone -> RegisterPhoneScreen(
            onCodeSent = { verId, number ->
                verificationId = verId
                phoneNumber = number
                currentScreen = Screen.VerifyCode
            },
            onBack = { currentScreen = Screen.Welcome }
        )

        // Pantalla para ingresar el código recibido por SMS
        is Screen.VerifyCode -> VerifyCodeScreen(
            verificationId = verificationId,
            phoneNumber = phoneNumber,
            onVerified = { currentScreen = Screen.EnterName },
            onBack = { currentScreen = Screen.RegisterPhone }
        )

        // Ingreso del nombre del usuario
        is Screen.EnterName -> EnterNameScreen(
            phoneNumber = phoneNumber,
            onBack = { currentScreen = Screen.VerifyCode },
            onNameEntered = { name ->
                val currentUser = FirebaseAuth.getInstance().currentUser

                // Validaciones básicas
                if (currentUser == null) {
                    Toast.makeText(context, "⚠ No se encontró sesión activa", Toast.LENGTH_SHORT).show()
                    currentScreen = Screen.Welcome
                    return@EnterNameScreen
                }
                if (name.isBlank()) {
                    Toast.makeText(context, "⚠ Ingresa tu nombre", Toast.LENGTH_SHORT).show()
                    return@EnterNameScreen
                }

                // Datos del usuario a guardar en Firestore
                val userData = mapOf(
                    "uid" to currentUser.uid,
                    "name" to name,
                    "phone" to (currentUser.phoneNumber ?: ""),
                    "profileImage" to "",
                    "status" to "Disponible",
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )

                // Guardar el usuario en Firebase
                db.collection("users").document(currentUser.uid)
                    .set(userData)
                    .addOnSuccessListener {
                        saveLoginState(context, true)
                        Toast.makeText(context, "✅ Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        currentScreen = Screen.Chats
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "❌ Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        // Pantalla de lista de chats
        is Screen.Chats -> ChatsScreen(
            onOpenChat = { chatId, chatName, chatType, participants ->
                // Navega al chat específico
                currentScreen = Screen.Chat(chatId, chatName, chatType, participants)
            },
            onOpenContacts = { currentScreen = Screen.Contacts },
            onAddContact = { currentScreen = Screen.AddContact },
            onOpenProfile = { currentScreen = Screen.Profile },
            onCreateGroup = { currentScreen = Screen.CreateGroup }
        )

        // Crear grupo
        is Screen.CreateGroup -> CreateGroupScreen(
            onBack = { currentScreen = Screen.Chats },
            onGroupCreated = { chatId, groupName, chatType, participants ->
                currentScreen = Screen.Chat(chatId, groupName, chatType, participants)
            }
        )

        // Lista de contactos
        is Screen.Contacts -> ContactsScreen(
            onBack = { currentScreen = Screen.Chats },
            onAddContact = { currentScreen = Screen.AddContact },
            onChatReady = { chatId, chatName, chatType, participants ->
                currentScreen = Screen.Chat(chatId, chatName, chatType, participants)
            }
        )

        // Agregar contacto nuevo
        is Screen.AddContact -> AddContactScreen(
            onBack = { currentScreen = Screen.Contacts }
        )

        // Perfil del usuario
        is Screen.Profile -> ProfileScreen(
            onBack = { currentScreen = Screen.Chats },
            onLogout = {
                saveLoginState(context, false)
                FirebaseAuth.getInstance().signOut()
                currentScreen = Screen.Welcome
            }
        )

        // Pantalla de chat individual o grupal
        is Screen.Chat -> ChatScreen(
            chatId = screen.chatId,
            chatName = screen.chatName,
            chatType = screen.chatType,
            participants = screen.participants,
            onBack = { currentScreen = Screen.Chats }
        )
    }
}
