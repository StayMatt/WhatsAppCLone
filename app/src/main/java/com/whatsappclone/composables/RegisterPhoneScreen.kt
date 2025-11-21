package com.whatsappclone.composables

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import com.whatsappclone.ui.theme.WhatsAppGreen
import com.whatsappclone.ui.theme.WhatsAppWhite
import com.whatsappclone.ui.theme.WhatsAppTextGray
import com.whatsappclone.ui.theme.WhatsAppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPhoneScreen(
    onCodeSent: (verificationId: String, phoneNumber: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()

    var phoneNumber by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Barra superior con botón para regresar
            TopAppBar(
                title = { Text("Registrar número", color = WhatsAppWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = WhatsAppWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppGreen)
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

            // Título principal
            Text(
                text = "Ingresa tu número telefónico",
                style = MaterialTheme.typography.headlineSmall,
                color = WhatsAppGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Texto informativo
            Text(
                text = "Recibirás un código de verificación para poder acceder a tus chats, contactos y grupos. ¡Es rápido y seguro!",
                style = MaterialTheme.typography.bodyMedium,
                color = WhatsAppTextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo para ingresar el número de teléfono
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    // Solo se permiten números y máximo 9 dígitos
                    if (input.length <= 9 && input.all { it.isDigit() }) {
                        phoneNumber = input
                    }
                },
                label = { Text("Número (9 dígitos)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Botón que envía el código de verificación
            Button(
                onClick = {
                    when {
                        phoneNumber.isBlank() -> {
                            mensaje = "Por favor ingresa un número válido."
                        }
                        phoneNumber.length != 9 -> {
                            mensaje = "El número debe tener exactamente 9 dígitos."
                        }
                        else -> {
                            // Formatear número con +51
                            val formattedNumber = if (phoneNumber.startsWith("+51")) phoneNumber else "+51$phoneNumber"

                            // Configurar opciones para Firebase Auth
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(formattedNumber)       // Número destino
                                .setTimeout(60L, TimeUnit.SECONDS)    // Tiempo límite
                                .setActivity(activity)                // Actividad actual
                                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                    // Verificación automática (a veces llega sin código)
                                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                                        mensaje = "Verificación automática completada ✅"
                                    }

                                    // Si algo falla (número inválido, SMS bloqueado, etc.)
                                    override fun onVerificationFailed(e: FirebaseException) {
                                        mensaje = "Error: ${e.message}"
                                    }

                                    // Cuando Firebase realmente envía el código
                                    override fun onCodeSent(
                                        verificationId: String,
                                        token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        mensaje = "Código enviado ✅"
                                        onCodeSent(verificationId, formattedNumber)
                                    }
                                })
                                .build()

                            // Enviar SMS
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar código", color = WhatsAppWhite)
            }

            // Texto con mensajes de estado
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
