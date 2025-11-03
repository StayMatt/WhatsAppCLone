package com.whatsappclone.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.model.Contact
import com.whatsappclone.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Contactsviewmodel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Carga los contactos del usuario y agrega info del usuario real
     */
    fun loadContacts(userId: String) {
        viewModelScope.launch {
            try {
                // 1️⃣ Obtener contactos guardados por el usuario
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("contacts")
                    .get()
                    .await()

                val contactList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Contact::class.java)
                }

                if (contactList.isEmpty()) {
                    _contacts.value = emptyList()
                    return@launch
                }

                // 2️⃣ Obtener todos los UIDs de usuarios registrados en la app
                val usersSnapshot = db.collection("users")
                    .get()
                    .await()

                val registeredUIDs = usersSnapshot.documents.map { it.id }.toSet()

                // 3️⃣ Marcar solo los contactos que existen en users
                val updatedContacts = contactList.map { contact ->
                    contact.copy(isRegistered = registeredUIDs.contains(contact.contactUserId))
                }

                _contacts.value = updatedContacts

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error al cargar contactos"
            }
        }
    }
    /**
     * Inicia o recupera un chat privado, evitando duplicados
     */
    /**
     * Inicia o recupera un chat privado, evitando duplicados
     * y asigna como nombre el nombre del contacto (Contact.name)
     */
    fun startOrGetPrivateChat(
        currentUserId: String,
        contactUserId: String,
        onResult: (chatId: String, chatName: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Buscar si ya existe un chat privado entre ambos usuarios
                val chatQuery = db.collection("chats")
                    .whereEqualTo("type", "private")
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()

                val existingChat = chatQuery.documents.firstOrNull { doc ->
                    val participants = doc.get("participants") as? List<*>
                    participants != null &&
                            participants.size == 2 &&
                            participants.contains(currentUserId) &&
                            participants.contains(contactUserId)
                }

                if (existingChat != null) {
                    // Chat ya existe
                    val chatId = existingChat.id
                    val chatName = existingChat.getString("name") ?: "Chat privado"
                    onResult(chatId, chatName)
                } else {
                    // 🟢 Obtener el contacto del usuario actual
                    val contactDoc = db.collection("users")
                        .document(currentUserId)
                        .collection("contacts")
                        .document(contactUserId)
                        .get()
                        .await()

                    val contact = contactDoc.toObject(Contact::class.java)
                    val contactName = contact?.name ?: "Chat privado" // 👈 variable declarada correctamente

                    // Crear nuevo chat privado
                    val newChatRef = db.collection("chats").document()
                    val chatData = mapOf(
                        "chatId" to newChatRef.id,
                        "type" to "private",
                        "participants" to listOf(currentUserId, contactUserId),
                        "name" to contactName, // ✅ ya existe la referencia
                        "lastMessage" to "",
                        "lastSenderId" to "",
                        "lastTimestamp" to System.currentTimeMillis(),
                        "createdBy" to currentUserId,
                        "createdAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )

                    newChatRef.set(chatData).await()
                    onResult(newChatRef.id, contactName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error al iniciar el chat"
                onResult("", "")
            }
        }
    }

    fun sendMessage(chatId: String, message: Message) {
        viewModelScope.launch {
            try {
                val chatRef = db.collection("chats").document(chatId)
                val messagesRef = chatRef.collection("messages").document() // Firestore genera un ID

                // Guardar el mensaje en la subcolección
                messagesRef.set(message).await()

                // Actualizar el último mensaje en el chat
                chatRef.update(
                    mapOf(
                        "lastMessage" to message.content,
                        "lastSenderId" to message.senderId,
                        "lastTimestamp" to message.timestamp,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error al enviar mensaje"
            }
        }
    }



}
