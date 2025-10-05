package com.whatsappclone.composables
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterNumberScreen(onRegistered: () -> Unit) {
    var number by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registrar número", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRegistered,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }
    }
}