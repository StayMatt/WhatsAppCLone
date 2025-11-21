package com.whatsappclone.composables

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.whatsappclone.viewmodel.Contactsviewmodel
import com.whatsappclone.model.Message
import com.whatsappclone.model.User
import com.whatsappclone.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    chatName: String,
    chatType: String = "private",
    participants: List<String> = listOf(),
    viewModel: Contactsviewmodel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // Lista de mensajes
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

    // Texto del input
    var inputText by remember { mutableStateOf("") }

    // Nombre del chat (cambia según contacto o grupo)
    var chatTitle by remember { mutableStateOf(chatName) }

    // Estado del usuario (simulado)
    var chatStatus by remember { mutableStateOf("En línea") }

    // Texto de "X te agregó"
    var groupAddedText by remember { mutableStateOf<String?>(null) }

    // Mapa con datos de usuarios (id → User)
    val usersMap = remember { mutableStateMapOf<String, User>() }

    // Permisos de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    // Imagen seleccionada antes de enviar
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador de permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    // Seleccionar imagen de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
        else Toast.makeText(context, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
    }

    // Tomar foto con cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Cuando se toma la foto, convertir a Base64 y enviar
        if (bitmap != null) {
            val base64Image = encodeImageToBase64(bitmap)
            val message = Message(
                chatId = chatId,
                senderId = currentUser.uid,
                senderName = usersMap[currentUser.uid]?.name ?: "Yo",
                content = base64Image,
                type = "image",
                timestamp = System.currentTimeMillis()
            )
            viewModel.sendMessage(chatId, message)
            Toast.makeText(context, "Foto enviada", Toast.LENGTH_SHORT).show()
        }
    }

    val messagesRef = db.collection("chats").document(chatId).collection("messages")
    val usersRef = db.collection("users")
    val chatsRef = db.collection("chats").document(chatId)

    // Cargar datos del contacto o grupo al abrir el chat
    LaunchedEffect(chatId) {
        try {
            if (chatType == "private") {
                // Obtiene ID del otro usuario
                val otherUserId =
                    participants.firstOrNull { it != currentUser.uid } ?: return@LaunchedEffect

                // Buscar contacto guardado localmente
                val contactSnap = db.collection("users")
                    .document(currentUser.uid)
                    .collection("contacts")
                    .whereEqualTo("contactUserId", otherUserId)
                    .get()
                    .await()

                val contactDoc = contactSnap.documents.firstOrNull()
                val contactName = contactDoc?.getString("name")
                val contactPhone = contactDoc?.getString("phone")

                // Cargar datos del usuario desde Firebase
                val snap = usersRef.document(otherUserId).get().await()
                val otherUser = snap.toObject(User::class.java) ?: User(uid = otherUserId)
                usersMap[otherUserId] = otherUser

                // Priorizar nombre guardado en contactos
                chatTitle = when {
                    !contactName.isNullOrBlank() -> contactName
                    !contactPhone.isNullOrBlank() -> contactPhone
                    else -> otherUser.phone.ifEmpty { "Usuario desconocido" }
                }

            } else {
                // Para chats grupales
                val chatSnap = chatsRef.get().await()
                chatTitle = chatSnap.getString("name") ?: "Grupo sin nombre"

                val membersList = chatSnap.get("participants") as? List<String> ?: emptyList()
                val createdBy = chatSnap.getString("createdBy") ?: ""

                // Mostrar aviso de quién te agregó
                if (createdBy.isNotEmpty()) {
                    val creatorUser =
                        usersRef.document(createdBy).get().await().toObject(User::class.java)

                    val contactSnap = db.collection("users")
                        .document(currentUser.uid)
                        .collection("contacts")
                        .whereEqualTo("contactUserId", createdBy)
                        .get()
                        .await()

                    val contactDoc = contactSnap.documents.firstOrNull()
                    val displayName = when {
                        !contactDoc?.getString("name").isNullOrBlank() -> contactDoc?.getString("name")
                        !creatorUser?.phone.isNullOrBlank() -> creatorUser?.phone
                        else -> "Alguien"
                    }
                    groupAddedText = if (createdBy != currentUser.uid)
                        "📢 $displayName te agregó al grupo"
                    else null
                }

                // Cargar nombres de cada miembro
                for (uid in membersList) {
                    val userSnap = usersRef.document(uid).get().await()
                    val user = userSnap.toObject(User::class.java) ?: User(uid = uid)

                    val contactSnap = db.collection("users")
                        .document(currentUser.uid)
                        .collection("contacts")
                        .whereEqualTo("contactUserId", uid)
                        .get()
                        .await()

                    val contactDoc = contactSnap.documents.firstOrNull()
                    val contactName = contactDoc?.getString("name")
                    val contactPhone = contactDoc?.getString("phone")

                    val displayName = when {
                        !contactName.isNullOrBlank() -> contactName
                        !contactPhone.isNullOrBlank() -> contactPhone
                        else -> user.phone.ifEmpty { "Usuario desconocido" }
                    }

                    usersMap[uid] = user.copy(name = displayName)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error cargando datos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Escuchar mensajes en tiempo real
    LaunchedEffect(chatId) {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar mensajes: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                // Convertir documentos a objetos Message
                messages = snapshot?.documents?.mapNotNull { it.toObject(Message::class.java) }
                    ?: emptyList()
            }
    }

    Scaffold(
        // Barra superior
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icono como foto de perfil
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = WhatsAppWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(chatTitle, color = WhatsAppWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(chatStatus, color = WhatsAppWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = WhatsAppWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Llamada */ }) {
                        Icon(Icons.Default.Call, contentDescription = "Llamar", tint = WhatsAppWhite)
                    }
                    IconButton(onClick = { /* Videollamada */ }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Videollamada", tint = WhatsAppWhite)
                    }
                    IconButton(onClick = { /* Menú */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más", tint = WhatsAppWhite)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = WhatsAppGreen)
            )
        },

        // Fondo estilo WhatsApp
        containerColor = WhatsAppBackground,

        // Barra inferior
        bottomBar = {
            Column {

                // Vista previa de imagen antes de enviar
                selectedImageUri?.let { uri ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(160.dp)
                        )
                        Column {
                            // Botón enviar imagen
                            Button(onClick = {
                                scope.launch {
                                    val inputStream: InputStream? =
                                        context.contentResolver.openInputStream(uri)
                                    val bytes = inputStream?.readBytes()
                                    if (bytes != null) {
                                        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                                        val msg = Message(
                                            chatId = chatId,
                                            senderId = currentUser.uid,
                                            senderName = usersMap[currentUser.uid]?.name ?: "Yo",
                                            content = base64,
                                            type = "image",
                                            timestamp = System.currentTimeMillis()
                                        )
                                        viewModel.sendMessage(chatId, msg)
                                        selectedImageUri = null
                                        Toast.makeText(
                                            context,
                                            "Imagen enviada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }) { Text("Enviar") }

                            OutlinedButton(onClick = { selectedImageUri = null }) {
                                Text("Cancelar")
                            }
                        }
                    }
                }

                // Caja de texto + botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhatsAppBackground)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Emoji */ }) {
                        Icon(
                            Icons.Default.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = WhatsAppTextGray
                        )
                    }

                    // Adjuntar imagen
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Adjuntar",
                            tint = WhatsAppTextGray
                        )
                    }

                    // Cámara
                    IconButton(onClick = {
                        if (!hasCameraPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        else cameraLauncher.launch(null)
                    }) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cámara",
                            tint = WhatsAppTextGray
                        )
                    }

                    // Input del mensaje
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Mensaje", color = WhatsAppTextGray) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = WhatsAppGreen,
                            focusedContainerColor = WhatsAppWhite,
                            unfocusedContainerColor = WhatsAppWhite,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    // Si no hay texto, mostrar micrófono
                    if (inputText.isBlank()) {
                        IconButton(onClick = { /* Micrófono */ }) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Micrófono",
                                tint = WhatsAppLightGreen
                            )
                        }
                    } else {
                        // Botón de enviar texto
                        IconButton(onClick = {
                            val message = Message(
                                chatId = chatId,
                                senderId = currentUser.uid,
                                senderName = usersMap[currentUser.uid]?.name ?: "Yo",
                                content = inputText.trim(),
                                type = "text",
                                timestamp = System.currentTimeMillis()
                            )
                            viewModel.sendMessage(chatId, message)
                            inputText = ""
                        }) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = WhatsAppLightGreen
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        // Lista de mensajes
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {

            // Mensaje "te agregaron al grupo"
            if (chatType == "group" && groupAddedText != null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = groupAddedText!!,
                            color = WhatsAppTextGray,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .background(Color(0xFFDADADA), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Mostrar todos los mensajes
            items(messages) { msg ->
                val isMe = msg.senderId == currentUser.uid
                val senderName = if (isMe) "Yo" else usersMap[msg.senderId]?.name ?: "Desconocido"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {

                        // Mostrar nombre del remitente si es un grupo
                        if (!isMe && chatType == "group") {
                            Text(senderName, fontSize = 10.sp, color = WhatsAppTextGray)
                        }

                        // Si el mensaje es una imagen
                        if (msg.type == "image") {
                            val decodedBitmap = remember(msg.content) {
                                try {
                                    val bytes = Base64.decode(msg.content, Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) { null }
                            }

                            // Mostrar imagen
                            if (decodedBitmap != null) {
                                Image(
                                    bitmap = decodedBitmap.asImageBitmap(),
                                    contentDescription = "Imagen recibida",
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(220.dp)
                                        .background(Color.LightGray, RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                )
                            } else {
                                Text("Error al mostrar imagen", color = Color.Red)
                            }

                        } else {
                            // Mensaje de texto
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isMe) Color(0xFF25A85D) else WhatsAppWhite,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(msg.content, color = if (isMe) WhatsAppWhite else Color.Black)
                            }
                        }

                        // Hora del mensaje
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                            fontSize = 10.sp,
                            color = WhatsAppTextGray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


// Convertir imagen a Base64 (para enviarla por Firestore)
fun encodeImageToBase64(bitmap: android.graphics.Bitmap): String {
    val baos = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}
