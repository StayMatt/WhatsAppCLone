package com.whatsappclone.model

data class Chat(
 val chatId: String = "",                 // ID del chat (Firestore lo genera)
 val type: String = "private",            // "private" o "group"
 val name: String = "",                   // Nombre del chat (para mostrar)
 val image: String = "",                  // Imagen del grupo o usuario
 val lastMessage: String = "",            // Último mensaje enviado
 val lastSenderId: String = "",           // ID del remitente del último mensaje
 val lastTimestamp: Long = 0L,            // Tiempo del último mensaje
 val participants: List<String> = emptyList(), // IDs de usuarios
 val createdBy: String = "",              // UID del creador
 val createdAt: Long = System.currentTimeMillis(),
 val updatedAt: Long = System.currentTimeMillis()
)
