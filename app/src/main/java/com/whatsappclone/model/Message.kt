package com.whatsappclone.model

data class Message(      val id: String = "",                 // ID del mensaje (Firestore lo puede generar)
                         val chatId: String = "",             // ID del chat o grupo
                         val senderId: String = "",           // UID del emisor
                         val senderName: String = "",         // Nombre del emisor (para grupos)
                         val content: String = "",            // Texto o URL si es imagen/audio
                         val type: String = "text",           // text, image, audio, video, file, etc.
                         val timestamp: Long = System.currentTimeMillis(), // Marca de tiempo
                         val seenBy: List<String> = listOf(), // UIDs que ya lo vieron (útil en grupos)
                         val repliedToId: String? = null,     // Si es una respuesta a otro mensaje
                         val deleted: Boolean = false  )

