package com.whatsappclone.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

// Firebase Auth para verificar el código
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

// Firestore para guardar el usuario si es nuevo
import com.google.firebase.firestore.FirebaseFirestore

// Colores del proyecto
import com.whatsappclone.ui.theme.WhatsAppGreen
import com.whatsappclone.ui.theme.WhatsAppWhite
import com.whatsappclone.ui.theme.WhatsAppTextGray
import com.whatsappclone.ui.theme.WhatsAppBackground


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    verificationId: String,   // ID generado al enviar el SMS
    phoneNumber: String,      // Número ingresado por el usuario
    onVerified: () -> Unit,   // Callback cuando el código es verificado
    onBack: () -> Unit        // Volver atrás
) {
    // Estado del código ingresado
    var code by remember { mutableStateOf("") }

    // Estado para mensajes de error o éxito
    var mensaje by remember { mutableStateOf("") }

    // Cargar o no el ProgressIndicator
    var isLoading by remember { mutableStateOf(false) }

    // Instancias de Firebase
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()


    // Pantalla con barra superior + contenido
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar código", color = WhatsAppWhite) },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = WhatsAppWhite
                        )
                    }
                },

                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = WhatsAppGreen
                )
            )
        },

        containerColor = WhatsAppBackground
    ) { padding ->

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Título
            Text(
                text = "Introduce el código de verificación",
                style = MaterialTheme.typography.headlineSmall,
                color = WhatsAppGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Hemos enviado un código SMS al número $phoneNumber. Ingresa los 6 dígitos para continuar y acceder a tus chats.",
                style = MaterialTheme.typography.bodyMedium,
                color = WhatsAppTextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo para ingresar el código SMS
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código SMS") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para verificar código
            Button(
                onClick = {

                    // Validación de longitud exacta
                    if (code.length == 6) {

                        // Mostrar cargando
                        isLoading = true

                        // Crear el credential con el código y el verificationId
                        val credential = PhoneAuthProvider.getCredential(verificationId, code)

                        // Intentar iniciar sesión con el credential
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->

                                // Detener animación
                                isLoading = false

                                if (task.isSuccessful) {
                                    // Usuario autenticado correctamente
                                    val user = auth.currentUser

                                    if (user != null) {

                                        // Referencia al documento del usuario
                                        val usersRef = firestore.collection("users").document(user.uid)

                                        // Verificar si el usuario ya existe
                                        usersRef.get()
                                            .addOnSuccessListener { snapshot ->

                                                if (!snapshot.exists()) {
                                                    // Si no existe → crearlo en Firestore
                                                    val userMap = mapOf(
                                                        "uid" to user.uid,
                                                        "phone" to phoneNumber,
                                                        "name" to (user.displayName ?: "")
                                                    )

                                                    usersRef.set(userMap)
                                                        .addOnSuccessListener {
                                                            mensaje = "Usuario registrado correctamente ✅"
                                                            onVerified()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            mensaje = "Error al guardar usuario: ${e.message}"
                                                        }
                                                } else {
                                                    // Si ya existe → solo continuar
                                                    mensaje = "Bienvenido de nuevo ✅"
                                                    onVerified()
                                                }

                                            }
                                            .addOnFailureListener { error ->
                                                mensaje = "Error al verificar usuario: ${error.message}"
                                            }
                                    }

                                } else {
                                    // Código incorrecto o expirado
                                    mensaje = task.exception?.localizedMessage ?: "Código incorrecto ❌"
                                }
                            }

                    } else {
                        mensaje = "Ingresa un código válido de 6 dígitos"
                    }
                },

                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                modifier = Modifier.fillMaxWidth()
            ) {

                // Si está cargando → mostrar círculo, sino texto
                if (isLoading)
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = WhatsAppWhite
                    )
                else
                    Text("Verificar", color = WhatsAppWhite)
            }

            // Mensaje de estado (error, éxito, etc.)
            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = mensaje,
                    color = WhatsAppGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
