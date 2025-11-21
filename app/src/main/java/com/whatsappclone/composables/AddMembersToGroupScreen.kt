package com.whatsappclone.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.model.Contact
import com.whatsappclone.ui.theme.*
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (chatId: String, groupName: String, chatType: String, participants: List<String>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return

    // Lista de contactos del usuario
    var contacts by remember { mutableStateOf(listOf<Contact>()) }

    // Lista de IDs seleccionados para el grupo
    val selectedUsers = remember { mutableStateListOf<String>() }

    // Nombre del grupo creado
    var groupName by remember { mutableStateOf("") }

    // Estado de carga
    var loading by remember { mutableStateOf(true) }

    // Cargar contactos del usuario al entrar a la pantalla
    LaunchedEffect(Unit) {
        try {
            val snap = db.collection("users")
                .document(currentUser.uid)
                .collection("contacts")
                .get()
                .await()

            contacts = snap.documents.mapNotNull { it.toObject(Contact::class.java) }
        } catch (e: Exception) {
            println("❌ Error obteniendo contactos: ${e.message}")
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Grupo", color = WhatsAppWhite) },
                navigationIcon = {
                    // Botón para volver atrás
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = WhatsAppWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhatsAppGreen)
            )
        },

        // Color de fondo general
        containerColor = WhatsAppBackground,

        // Botón inferior para crear el grupo
        bottomBar = {
            Button(
                onClick = {
                    // Validación básica
                    if (groupName.isEmpty()) {
                        Toast.makeText(context, "Ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedUsers.isEmpty()) {
                        Toast.makeText(context, "Selecciona al menos un miembro", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Agregar al creador como miembro del grupo
                    val members = selectedUsers.toMutableList().apply { add(currentUser.uid) }

                    // Crear ID único para el grupo
                    val chatRef = db.collection("chats").document()

                    // Datos del nuevo grupo
                    val data = mapOf(
                        "chatId" to chatRef.id,
                        "name" to groupName,
                        "type" to "group",
                        "image" to "",
                        "participants" to members,
                        "lastMessage" to "",
                        "lastSenderId" to "",
                        "lastTimestamp" to System.currentTimeMillis(),
                        "createdBy" to currentUser.uid,
                        "createdAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )

                    // Guardar el grupo en Firestore
                    chatRef.set(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Grupo creado correctamente", Toast.LENGTH_SHORT).show()
                            onGroupCreated(chatRef.id, groupName, "group", members)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al crear grupo", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                colors = ButtonDefaults.buttonColors(WhatsAppGreen)
            ) {
                Text("Crear Grupo", color = WhatsAppWhite, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->

        // Loading inicial
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WhatsAppGreen)
            }
            return@Scaffold
        }

        // Si no tiene contactos guardados
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes contactos registrados", color = WhatsAppTextGray)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            // Campo de nombre del grupo
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Nombre del grupo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Selecciona miembros",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(12.dp))

            // Lista de contactos del usuario para selección
            LazyColumn {
                items(contacts) { contact ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Agregar o quitar usuario de la lista seleccionada
                                val uid = contact.contactUserId
                                if (selectedUsers.contains(uid)) selectedUsers.remove(uid)
                                else selectedUsers.add(uid)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Checkbox de selección
                        Checkbox(
                            checked = selectedUsers.contains(contact.contactUserId),
                            onCheckedChange = { checked ->
                                if (checked) selectedUsers.add(contact.contactUserId)
                                else selectedUsers.remove(contact.contactUserId)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = WhatsAppGreen)
                        )

                        Spacer(Modifier.width(10.dp))

                        // Nombre del contacto o su teléfono si no tiene nombre
                        Text(
                            text = contact.name.ifEmpty { contact.phone },
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
