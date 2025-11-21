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

/**
 * ViewModel encargado de:
 *  - Obtener contactos del usuario desde Firestore
 *  - Verificar si esos contactos están registrados en la app
 *  - Iniciar o recuperar chats privados
 *  - Enviar mensajes y actualizar el último mensaje
 *
 *  Toda la lógica de datos relacionada a CONTACTOS y CHATS está aquí.
 */
class Contactsviewmodel : ViewModel() {

    // Referencia a Firestore
    private val db = FirebaseFirestore.getInstance()

    // Flujo que contiene la lista de contactos del usuario
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    // Flujo que guarda mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    /**
     * 🔹 loadContacts()
     * Carga los contactos guardados por el usuario en Firestore
     * Luego verifica cuáles de esos contactos ESTÁN registrados en la app (colección "users")
     */
    fun loadContacts(userId: String) {
        viewModelScope.launch {
            try {

                // 1️⃣ Obtener la lista de contactos guardados por el usuario actual
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("contacts")
                    .get()
                    .await()

                // Convertir cada documento en un objeto Contact
                val contactList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Contact::class.java)
                }

                // Si no tiene contactos → vaciamos la lista
                if (contactList.isEmpty()) {
                    _contacts.value = emptyList()
                    return@launch
                }

                // 2️⃣ Obtener todos los usuarios existentes registrados en la app
                val usersSnapshot = db.collection("users")
                    .get()
                    .await()

                // Extraer sus UIDs para verificar coincidencias
                val registeredUIDs = usersSnapshot.documents.map { it.id }.toSet()

                // 3️⃣ Verificar cuáles de los contactos están registrados
                val updatedContacts = contactList.map { contact ->
                    contact.copy(isRegistered = registeredUIDs.contains(contact.contactUserId))
                }

                // Actualizar flujo de contactos en UI
                _contacts.value = updatedContacts

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error al cargar contactos"
            }
        }
    }


    /**
     * 🔹 startOrGetPrivateChat()
     *
     * Inicia o recupera un chat privado entre dos usuarios.
     * - Evita duplicados (si ya existe un chat, lo usa)
     * - Asigna como nombre del chat el nombre del contacto
     * - Crea un chat nuevo si no existe
     */
    fun startOrGetPrivateChat(
        currentUserId: String,
        contactUserId: String,
        onResult: (chatId: String, chatName: String) -> Unit
    ) {
        viewModelScope.launch {
            try {

                // 1️⃣ Buscar si ya existe un chat privado entre ambos usuarios
                val chatQuery = db.collection("chats")
                    .whereEqualTo("type", "private")
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()

                // El chat solo es válido si tiene EXACTAMENTE los dos participantes
                val existingChat = chatQuery.documents.firstOrNull { doc ->
                    val participants = doc.get("participants") as? List<*>
                    participants != null &&
                            participants.size == 2 &&
                            participants.contains(currentUserId) &&
                            participants.contains(contactUserId)
                }

                // 2️⃣ Si existe → devolverlo
                if (existingChat != null) {
                    val chatId = existingChat.id
                    val chatName = existingChat.getString("name") ?: "Chat privado"
                    onResult(chatId, chatName)
                    return@launch
                }


                // 3️⃣ Obtener el nombre del contacto para el chat
                val contactDoc = db.collection("users")
                    .document(currentUserId)
                    .collection("contacts")
                    .document(contactUserId)
                    .get()
                    .await()

                val contact = contactDoc.toObject(Contact::class.java)

                // Nombre del chat = nombre del contacto
                val contactName = contact?.name ?: "Chat privado"

                // 4️⃣ Crear un nuevo chat privado
                val newChatRef = db.collection("chats").document()

                // Datos base del chat
                val chatData = mapOf(
                    "chatId" to newChatRef.id,
                    "type" to "private",
                    "participants" to listOf(currentUserId, contactUserId),
                    "name" to contactName,
                    "lastMessage" to "",
                    "lastSenderId" to "",
                    "lastTimestamp" to System.currentTimeMillis(),
                    "createdBy" to currentUserId,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )

                // Guardar en Firestore
                newChatRef.set(chatData).await()

                // Devolver el nuevo chat
                onResult(newChatRef.id, contactName)

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error al iniciar el chat"
                onResult("", "")
            }
        }
    }


    /**
     * 🔹 sendMessage()
     *
     * Envía un mensaje a un chat y actualiza:
     *   - último mensaje
     *   - ID del último remitente
     *   - timestamp
     */
    fun sendMessage(chatId: String, message: Message) {
        viewModelScope.launch {
            try {

                // Referencia al documento del chat
                val chatRef = db.collection("chats").document(chatId)

                // Subcolección "messages"
                val messageRef = chatRef.collection("messages").document()

                // Guardar mensaje
                messageRef.set(message).await()

                // Actualizar datos importantes del chat
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
