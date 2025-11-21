package com.whatsappclone.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.whatsappclone.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    // Variables para almacenar datos del usuario
    var name by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    // 🔹 Cargar información del usuario desde Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        name = snapshot.getString("name")
                        phone = snapshot.getString("phone")
                    } else {
                        // Si no hay documento, usar info básica del auth
                        name = null
                        phone = currentUser.phoneNumber ?: "Sin teléfono"
                    }
                    loading = false
                }
                .addOnFailureListener {
                    // Error al consultar Firestore
                    name = "Error al cargar"
                    phone = "-"
                    loading = false
                }
        } ?: run {
            // No hubo usuario autenticado
            name = "Usuario no autenticado"
            phone = "-"
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppWhite
                    )
                },
                // Botón de volver
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = WhatsAppWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = WhatsAppGreen
                )
            )
        },
        containerColor = WhatsAppWhite
    ) { padding ->

        // Mostrar indicador de carga si aún no se han obtenido datos
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WhatsAppGreen)
            }
        } else {
            // 🟩 Contenido principal del perfil
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Tarjeta con foto inicial, nombre y teléfono
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WhatsAppGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // 🔹 Avatar con inicial del nombre o número
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(WhatsAppWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (name?.takeIf { it.isNotBlank() } ?: phone ?: "?")
                                    .take(1)
                                    .uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 🔹 Nombre del usuario
                        Text(
                            text = name?.takeIf { it.isNotBlank() } ?: phone ?: "Sin información",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppWhite
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 🔹 Teléfono del usuario
                        Text(
                            text = phone ?: "Sin teléfono",
                            fontSize = 16.sp,
                            color = WhatsAppWhite
                        )
                    }
                }

                // 🔹 Botón para cerrar sesión
                Button(
                    onClick = {
                        val preferences = context.getSharedPreferences("my_prefs", android.content.Context.MODE_PRIVATE)
                        preferences.edit().putBoolean("ESTA_LOGUEADO", false).apply()

                        FirebaseAuth.getInstance().signOut()
                        onLogout?.invoke()

                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = WhatsAppWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
