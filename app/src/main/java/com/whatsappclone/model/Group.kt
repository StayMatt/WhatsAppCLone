package com.whatsappclone.model

data class Group(
    val groupId: String = "",                    // Igual al chatId
    val name: String = "",                       // Nombre del grupo
    val image: String = "",                      // Imagen del grupo
    val description: String = "",                // Descripción opcional
    val createdBy: String = "",                  // UID del creador
    val admins: List<String> = listOf(),         // IDs de administradores
    val members: List<String> = listOf(),        // IDs de los miembros
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
