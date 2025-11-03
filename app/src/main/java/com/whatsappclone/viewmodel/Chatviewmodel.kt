package com.whatsappclone.viewmodel


import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.whatsappclone.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Chatviewmodel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    fun loadMessages(chatId: String) {
        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _messages.value = snapshot.toObjects(Message::class.java)
                }
            }
    }

    fun sendMessage(message: Message) {
        val ref = db.collection("chats").document(message.chatId)
            .collection("messages")
        ref.add(message)
    }
}
