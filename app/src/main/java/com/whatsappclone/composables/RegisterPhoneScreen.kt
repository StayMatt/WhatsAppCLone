package com.whatsappclone.composables


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RegisterPhoneScreen(onPhoneEntered: (String) -> Unit, onBack: () -> Unit) {
    var phone by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar número") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Número de teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { onPhoneEntered(phone) }, enabled = phone.isNotBlank()) {
                Text("Continuar")
            }
        }
    }
}