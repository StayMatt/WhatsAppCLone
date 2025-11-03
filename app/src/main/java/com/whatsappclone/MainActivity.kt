package com.whatsappclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.composables.MainScreen
import com.whatsappclone.ui.theme.WhatsAppCLoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inicializa Firebase correctamente
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ✅ Asegura la conexión con Firestore (inicializa antes del UI)
        val db = FirebaseFirestore.getInstance()

        // (Opcional pero recomendado) Configurar caché offline
        db.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        // ✅ Verifica sesión activa antes de cargar la interfaz
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            println("🔥 Sesión activa con: ${currentUser.phoneNumber ?: "sin número"}")
        } else {
            println("🚪 No hay sesión activa, mostrando pantalla de bienvenida.")
        }

        // ✅ Cargar interfaz principal
        setContent {
            WhatsAppCLoneTheme {
                MainScreen()
            }
        }
    }
}
