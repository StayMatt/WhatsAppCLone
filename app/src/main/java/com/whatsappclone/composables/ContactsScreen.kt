package com.whatsappclone.composables

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.whatsappclone.viewmodel.Contactsviewmodel
import com.whatsappclone.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: Contactsviewmodel = viewModel(),
    onBack: () -> Unit,
    onAddContact: () -> Unit,
    onChatReady: (chatId: String, chatName: String, chatType: String, participants: List<String>) -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return

    // Estado proveniente del ViewModel
    val contacts by viewModel.contacts.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 🔹 Cargar contactos al entrar a la pantalla
    LaunchedEffect(currentUser.uid) {
        viewModel.loadContacts(currentUser.uid)
    }

    // 🔹 Mostrar errores (si ocurre algo en Firestore)
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos", fontWeight = FontWeight.Bold, color = WhatsAppWhite) },

                // 🔹 Botón para volver atrás
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = WhatsAppWhite)
                    }
                },

                // 🔹 Botón para agregar contactos manualmente
                actions = {
                    IconButton(onClick = onAddContact) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar contacto", tint = WhatsAppWhite)
                    }
                },

                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = WhatsAppGreen)
            )
        },

        containerColor = WhatsAppBackground
    ) { padding ->
        // 🔹 Lista completa de contactos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            items(contacts) { contact ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            // Evitar abrir chat con usuarios NO registrados
                            if (!contact.isRegistered) {
                                Toast.makeText(
                                    context,
                                    "El contacto no está registrado en la app",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@clickable
                            }

                            // 🔹 Crear o abrir un chat privado usando el ViewModel
                            viewModel.startOrGetPrivateChat(
                                currentUser.uid,
                                contact.contactUserId
                            ) { chatId, chatName ->

                                if (chatId.isNotEmpty()) {
                                    onChatReady(
                                        chatId,
                                        chatName,
                                        "private",
                                        listOf(currentUser.uid, contact.contactUserId)
                                    )
                                } else {
                                    Toast.makeText(context, "No se pudo abrir el chat", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    // 🔹 Avatar circular
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(WhatsAppLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.profileImage.isNotBlank()) {
                            // Si tiene imagen, mostrarla
                            Image(
                                painter = rememberAsyncImagePainter(contact.profileImage),
                                contentDescription = contact.name,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            // Si NO tiene imagen, mostrar inicial
                            Text(
                                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                                color = WhatsAppWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 🔹 Nombre, teléfono y estado
                    Column(modifier = Modifier.weight(1f)) {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(contact.phone, fontSize = 14.sp, color = WhatsAppTextGray)

                        if (contact.status.isNotBlank()) {
                            Text(contact.status, fontSize = 12.sp, color = WhatsAppTextGray)
                        }
                    }

                    // Flecha indicando que se puede abrir chat
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Ir al chat",
                        tint = if (contact.isRegistered) WhatsAppGreen else Color.Gray
                    )
                }

                Divider()
            }
        }
    }
}
