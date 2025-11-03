package com.whatsappclone.model

data class Contact(
    val contactId: String = "",          // ID único del contacto (puede ser UID del usuario o generado)
    val userId: String = "",             // UID del usuario que tiene este contacto guardado
    val contactUserId: String = "",      // UID del usuario al que pertenece este contacto
    val name: String = "",               // Nombre que el usuario le asigna al contacto
    val phone: String = "",              // Número de teléfono del contacto
    val profileImage: String = "",       // URL de la foto de perfil (sincronizada con User)
    val status: String = "",             // Estado tipo “Disponible”, “Ocupado”, etc.
    val isRegistered: Boolean = false,   // Si el contacto ya está registrado en la app
    val addedAt: Long = System.currentTimeMillis() // Fecha de agregado
)
