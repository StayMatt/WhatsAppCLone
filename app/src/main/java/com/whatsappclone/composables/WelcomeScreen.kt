package com.whatsappclone.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun WelcomeScreen(onAccept: () -> Unit) {
    var termsAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a HsatApp",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
            Text("Acepto los Términos y Condiciones")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (termsAccepted) onAccept() },
            enabled = termsAccepted
        ) {
            Text("Aceptar")
        }
    }
}