package com.whatsappclone.composables

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.model.Group
import com.whatsappclone.model.User
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

    var users by remember { mutableStateOf(listOf<User>()) }
    val selectedUsers = remember { mutableStateListOf<String>() }
    var groupName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    // 🔹 Cargar todos los usuarios
    LaunchedEffect(Unit) {
        val snapshot = db.collection("users").get().await()
        users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            .filter { it.uid != currentUser.uid }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Grupo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (groupName.isBlank()) {
                        Toast.makeText(context, "Ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedUsers.isEmpty()) {
                        Toast.makeText(context, "Selecciona al menos un miembro", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val membersList = selectedUsers.toMutableList().apply { add(currentUser.uid) }
                    val chatRef = db.collection("chats").document()

                    val chatData = mapOf(
                        "chatId" to chatRef.id,
                        "type" to "group",
                        "name" to groupName,
                        "image" to "", // puedes agregar imagen opcional del grupo
                        "participants" to membersList,
                        "lastMessage" to "",
                        "lastSenderId" to "",
                        "lastTimestamp" to System.currentTimeMillis(),
                        "createdBy" to currentUser.uid,
                        "createdAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )

                    chatRef.set(chatData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Grupo creado correctamente", Toast.LENGTH_SHORT).show()
                            // ⚡ Pasamos chatType y participants al abrir el chat
                            onGroupCreated(chatRef.id, groupName, "group", membersList)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al crear chat: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Crear Grupo")
            }
        }
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nombre del grupo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Selecciona miembros:", style = MaterialTheme.typography.bodyMedium)

                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(users) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedUsers.contains(user.uid)) selectedUsers.remove(user.uid)
                                    else selectedUsers.add(user.uid)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedUsers.contains(user.uid),
                                onCheckedChange = { checked ->
                                    if (checked) selectedUsers.add(user.uid)
                                    else selectedUsers.remove(user.uid)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.name.ifEmpty { user.phone })
                        }
                    }
                }
            }
        }
    }
}



