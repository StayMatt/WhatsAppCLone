package com.whatsappclone.model

data class Chat(
    val chatId: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
