package com.whatsappclone.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ChatsScreen(
    onOpenChat: (String) -> Unit,
    onAddContact: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Clone") },
                actions = {
                    IconButton(onClick = onAddContact) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar contacto")
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Chat con María", Modifier.clickable { onOpenChat("1") })
            Spacer(modifier = Modifier.height(8.dp))
            Text("Chat con José", Modifier.clickable { onOpenChat("2") })
        }
    }
}