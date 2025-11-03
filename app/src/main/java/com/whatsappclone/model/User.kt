package com.whatsappclone.model

data class User(
    val uid: String = "",
    val phone: String = "",
    val name: String = "",
    val profileImage: String = "",     // URL de la foto de perfil
    val status: String = "",           // Estado tipo “Disponible”, “Ocupado”, etc.
    val lastSeen: Long = 0,            // Timestamp del último acceso
    val online: Boolean = false
)
