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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.ui.theme.WhatsAppGreen
import com.whatsappclone.ui.theme.WhatsAppTextGray
import com.whatsappclone.ui.theme.WhatsAppWhite

// Colores estilo WhatsApp


@Composable
fun EnterNameScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onNameEntered: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¡Casi listo!",
            style = MaterialTheme.typography.headlineSmall,
            color = WhatsAppGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Para comenzar a chatear con tus amigos, necesitamos conocer tu nombre completo. Esto nos ayudará a mostrar tu perfil correctamente en los chats.",
            style = MaterialTheme.typography.bodyMedium,
            color = WhatsAppTextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tu nombre completo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = WhatsAppGreen,
                unfocusedIndicatorColor = WhatsAppTextGray,
                cursorColor = WhatsAppGreen,
                focusedLabelColor = WhatsAppGreen,
                unfocusedLabelColor = WhatsAppTextGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    isLoading = true
                    val user = auth.currentUser
                    if (user != null) {
                        firestore.collection("users")
                            .document(user.uid)
                            .update("name", name)
                            .addOnSuccessListener {
                                isLoading = false
                                mensaje = "Nombre guardado correctamente ✅"
                                onNameEntered(name)
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                mensaje = "Error al guardar el nombre: ${e.message}"
                            }
                    } else {
                        mensaje = "Error: usuario no autenticado"
                    }
                } else {
                    mensaje = "Por favor, ingresa tu nombre"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading)
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WhatsAppWhite)
            else
                Text("Continuar", color = WhatsAppWhite)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Número registrado: $phoneNumber",
            style = MaterialTheme.typography.bodySmall,
            color = WhatsAppTextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Atrás", color = WhatsAppGreen)
        }

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mensaje,
                color = WhatsAppGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
