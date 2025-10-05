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
fun VerifyCodeScreen(correctCode: String, onVerified: () -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tu código de verificación es:")
            Text(
                text = correctCode,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    error = false
                },
                label = { Text("Ingrese el código") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                if (code == correctCode) onVerified()
                else error = true
            }) {
                Text("Verificar")
            }

            if (error) {
                Text(
                    text = "Código incorrecto",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}