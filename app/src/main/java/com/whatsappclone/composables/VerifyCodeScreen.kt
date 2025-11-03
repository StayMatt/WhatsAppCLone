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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.ui.theme.WhatsAppGreen
import com.whatsappclone.ui.theme.WhatsAppWhite
import com.whatsappclone.ui.theme.WhatsAppTextGray
import com.whatsappclone.ui.theme.WhatsAppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    verificationId: String,
    phoneNumber: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar código", color = WhatsAppWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = WhatsAppWhite)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = WhatsAppGreen
                )
            )
        },
        containerColor = WhatsAppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Introduce el código de verificación",
                style = MaterialTheme.typography.headlineSmall,
                color = WhatsAppGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hemos enviado un código SMS al número $phoneNumber. Ingresa los 6 dígitos para continuar y acceder a tus chats.",
                style = MaterialTheme.typography.bodyMedium,
                color = WhatsAppTextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código SMS") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (code.length == 6) {
                        isLoading = true
                        val credential = PhoneAuthProvider.getCredential(verificationId, code)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        val usersRef = firestore.collection("users").document(user.uid)
                                        usersRef.get().addOnSuccessListener { snapshot ->
                                            if (!snapshot.exists()) {
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
                                                mensaje = "Bienvenido de nuevo ✅"
                                                onVerified()
                                            }
                                        }.addOnFailureListener { error ->
                                            mensaje = "Error al verificar usuario: ${error.message}"
                                        }
                                    }
                                } else {
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
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WhatsAppWhite)
                else Text("Verificar", color = WhatsAppWhite)
            }

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
