package com.whatsappclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.whatsappclone.composables.AddContactScreen
import com.whatsappclone.composables.ChatScreen
import com.whatsappclone.composables.ChatsScreen
import com.whatsappclone.composables.MainScreen
import com.whatsappclone.composables.RegisterNumberScreen
import com.whatsappclone.ui.theme.WhatsAppCLoneTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsAppCLoneTheme {
                MainScreen()
            }
        }
    }
}