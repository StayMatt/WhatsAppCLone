package com.whatsappclone.composables
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar contacto") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Registra un nuevo contacto", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = numero,
                onValueChange = { numero = it },
                label = { Text("Número de teléfono") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && apellido.isNotBlank() && numero.isNotBlank()) {
                        val database = FirebaseDatabase.getInstance()
                        val contactsRef = database.getReference("contacts")

                        val id = UUID.randomUUID().toString()
                        val contact = mapOf(
                            "id" to id,
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "numero" to numero
                        )

                        contactsRef.child(id).setValue(contact)
                            .addOnSuccessListener {
                                mensaje = "Contacto registrado correctamente "
                                nombre = ""
                                apellido = ""
                                numero = ""
                            }
                            .addOnFailureListener {
                                mensaje = "Error al registrar el contacto "
                            }
                    } else {
                        mensaje = "Por favor, completa todos los campos"
                    }
                },
                enabled = nombre.isNotBlank() && apellido.isNotBlank() && numero.isNotBlank()
            ) {
                Text("Guardar contacto")
            }

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(mensaje, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
