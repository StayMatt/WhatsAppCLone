package com.whatsappclone.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputField(
    onSendText: (String) -> Unit,
    onPickImage: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        IconButton(onClick = onPickImage) {
            Icon(Icons.Default.Photo, contentDescription = "Imagen")
        }

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un mensaje...") }
        )

        IconButton(onClick = {
            onSendText(text)
            text = ""
        }) {
            Icon(Icons.Default.Send, contentDescription = "Enviar")
        }
    }
}
