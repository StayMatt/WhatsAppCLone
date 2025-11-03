package com.whatsappclone.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.model.Chat
import com.whatsappclone.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onOpenChat: (chatId: String, chatName: String, chatType: String, participants: List<String>) -> Unit,
    onOpenContacts: () -> Unit,
    onAddContact: () -> Unit,
    onCreateGroup: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var chats by remember { mutableStateOf(listOf<Chat>()) }
    var showMenu by remember { mutableStateOf(false) }

    // 🔹 Listener de chats
    DisposableEffect(Unit) {
        val listener = db.collection("chats")
            .whereArrayContains("participants", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    chats = emptyList()
                    return@addSnapshotListener
                }

                scope.launch {
                    val tempList = mutableListOf<Chat>()
                    val usersCollection = db.collection("users")

                    for (doc in snapshot.documents) {
                        val chat = doc.toObject(Chat::class.java)?.copy(chatId = doc.id) ?: continue

                        if (chat.type == "private") {
                            val contactId = chat.participants.firstOrNull { it != currentUser.uid }
                            var displayName = "Usuario desconocido"
                            var profileImage = ""

                            if (!contactId.isNullOrBlank()) {
                                try {
                                    val contactSnap = db.collection("users")
                                        .document(currentUser.uid)
                                        .collection("contacts")
                                        .whereEqualTo("contactUserId", contactId)
                                        .get()
                                        .await()

                                    val contactDoc = contactSnap.documents.firstOrNull()
                                    val contactName = contactDoc?.getString("name")
                                    val contactPhone = contactDoc?.getString("phone")

                                    val userSnap = usersCollection.document(contactId.trim()).get().await()
                                    profileImage = userSnap.getString("profileImage") ?: ""

                                    displayName = when {
                                        !contactName.isNullOrBlank() -> contactName
                                        !contactPhone.isNullOrBlank() -> contactPhone
                                        else -> userSnap.getString("phone") ?: "Usuario desconocido"
                                    }
                                } catch (_: Exception) {}
                            }

                            tempList.add(
                                chat.copy(
                                    name = displayName,
                                    image = profileImage
                                )
                            )
                        } else {
                            tempList.add(chat) // chat grupal
                        }
                    }

                    chats = tempList.sortedByDescending { it.lastTimestamp }
                }
            }

        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsAppClone", color = WhatsAppWhite, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenContacts) { Icon(Icons.Default.Message, contentDescription = "Contactos", tint = WhatsAppWhite) }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) { Icon(Icons.Default.Add, contentDescription = "Más opciones", tint = WhatsAppWhite) }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Agregar contacto") }, onClick = {
                                showMenu = false
                                onAddContact()
                            })
                            DropdownMenuItem(text = { Text("Crear grupo") }, onClick = {
                                showMenu = false
                                onCreateGroup()
                            })
                        }
                    }
                    IconButton(onClick = onOpenProfile) { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = WhatsAppWhite) }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = WhatsAppGreen)
            )
        },
        containerColor = WhatsAppBackground
    ) { padding ->
        if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes chats activos", color = WhatsAppTextGray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(chats) { chat ->
                    ChatItem(chat = chat) {
                        onOpenChat(chat.chatId, chat.name, chat.type, chat.participants)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val hasUnread = chat.lastSenderId != currentUserId && chat.lastMessage.isNotBlank()

    // 🔹 Detectar tipo de mensaje
    val lastMessageDisplay = if (chat.lastMessage.startsWith("/9")) {
        "📷 Foto"
    } else {
        chat.lastMessage
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con borde
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (chat.image.isNotBlank()) Color.LightGray else WhatsAppGreen)
                .border(
                    width = if (hasUnread) 2.dp else 0.dp,
                    color = if (hasUnread) WhatsAppGreen else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (chat.image.isNotBlank()) {
                AsyncImage(
                    model = chat.image,
                    contentDescription = chat.name,
                    modifier = Modifier.clip(CircleShape)
                )
            } else {
                Text(
                    chat.name.firstOrNull()?.uppercase() ?: "?",
                    color = WhatsAppWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    chat.name,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                if (chat.lastTimestamp > 0L) {
                    val timeText = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(chat.lastTimestamp))
                    Text(
                        timeText,
                        fontSize = 12.sp,
                        color = WhatsAppTextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                lastMessageDisplay,
                fontSize = 14.sp,
                color = if (hasUnread) Color.Black else WhatsAppTextGray,
                maxLines = 1
            )
        }

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(WhatsAppGreen, shape = CircleShape)
            )
        }
    }
}
