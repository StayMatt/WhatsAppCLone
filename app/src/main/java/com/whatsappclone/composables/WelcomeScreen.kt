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

    // Estado para saber si el usuario aceptó los términos
    var termsAccepted by remember { mutableStateOf(false) }

    // Color verde típico de WhatsApp
    val whatsappGreen = Color(0xFF075E54)


    // Caja principal que contiene toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(whatsappGreen),  // Fondo verde
        contentAlignment = Alignment.Center
    ) {

        // Columna para acomodar el contenido en el centro
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {

            // Título principal de bienvenida
            Text(
                text = "¡Bienvenido a WhatsAppClone!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo explicando la app
            Text(
                text = "Conéctate con tus amigos y familiares de manera rápida y segura.\n" +
                        "Envía mensajes, imágenes y comparte momentos especiales.",
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de términos y condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox para que el usuario acepte los términos
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it }, // Cambia valor
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        uncheckedColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Texto de los términos
                Text(
                    text = "Acepto los Términos y Condiciones",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para continuar
            Button(
                onClick = {
                    // Solo permite avanzar si aceptó los términos
                    if (termsAccepted) onAccept()
                },
                enabled = termsAccepted,   // Se desactiva si no acepta
                shape = RoundedCornerShape(24.dp),  // Bordes redondeados
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White // Botón blanco
                )
            ) {
                Text(
                    text = "Comenzar",
                    color = whatsappGreen,      // Texto verde
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
