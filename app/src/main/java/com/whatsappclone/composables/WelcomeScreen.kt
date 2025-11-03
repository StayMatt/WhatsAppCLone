package com.whatsappclone.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onAccept: () -> Unit) {
    var termsAccepted by remember { mutableStateOf(false) }

    // Color clásico de WhatsApp
    val whatsappGreen = Color(0xFF075E54)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(whatsappGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Título principal
            Text(
                text = "¡Bienvenido a WhatsAppClone!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = "Conéctate con tus amigos y familiares de manera rápida y segura.\n" +
                        "Envía mensajes, imágenes y comparte momentos especiales.",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Términos y Condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color.White, uncheckedColor = Color.White)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acepto los Términos y Condiciones",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para continuar
            Button(
                onClick = { if (termsAccepted) onAccept() },
                enabled = termsAccepted,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Comenzar",
                    color = whatsappGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
