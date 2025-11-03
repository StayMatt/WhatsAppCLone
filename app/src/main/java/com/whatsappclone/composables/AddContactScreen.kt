package com.whatsappclone.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.model.Contact
import com.whatsappclone.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Contacto", fontWeight = FontWeight.Bold, color = WhatsAppWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = WhatsAppWhite)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = WhatsAppGreen)
            )
        },
        containerColor = WhatsAppWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Agrega un contacto para empezar a chatear con él o ella en WhatsAppClone. ¡Es rápido y seguro!",
                color = WhatsAppTextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de teléfono (solo números y máximo 9 dígitos)
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    if (input.length <= 9 && input.all { it.isDigit() }) phone = input
                },
                label = { Text("Número de teléfono") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de agregar contacto
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.length == 9) {
                        val fullPhone = "+51$phone" // 🔹 Agregar prefijo internacional
                        val contactId = UUID.randomUUID().toString()

                        // Verificar si el número está registrado
                        db.collection("users")
                            .whereEqualTo("phone", fullPhone)
                            .get()
                            .addOnSuccessListener { query ->
                                val isRegistered = !query.isEmpty
                                val contactUserId =
                                    if (isRegistered) query.documents.first().getString("uid") ?: "" else ""

                                val contact = Contact(
                                    contactId = contactId,
                                    userId = currentUser.uid,
                                    contactUserId = contactUserId,
                                    name = name,
                                    phone = fullPhone, // 🔹 Guardar con +51
                                    profileImage = "",
                                    status = if (isRegistered) "Disponible" else "No registrado",
                                    isRegistered = isRegistered,
                                    addedAt = System.currentTimeMillis()
                                )

                                // Guardar contacto en la subcolección "contacts" del usuario
                                db.collection("users")
                                    .document(currentUser.uid)
                                    .collection("contacts")
                                    .document(contactId)
                                    .set(contact)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Contacto agregado ✅", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al verificar el contacto ⚠", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Verifica los datos (9 dígitos y nombre)", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Agregar Contacto",
                    color = WhatsAppWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
