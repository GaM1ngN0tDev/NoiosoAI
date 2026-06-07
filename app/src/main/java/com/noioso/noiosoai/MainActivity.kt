package com.noioso.noiosoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noioso.noiosoai.data.local.SettingsManager
import com.noioso.noiosoai.data.repository.ChatRepository
import com.noioso.noiosoai.ui.chat.ChatScreen
import com.noioso.noiosoai.ui.chat.ChatViewModel
import com.noioso.noiosoai.ui.settings.SettingsScreen
import com.noioso.noiosoai.ui.theme.NoiosoAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Proper edge-to-edge for full screen immersive experience
        enableEdgeToEdge()
        
        val settingsManager = SettingsManager(applicationContext)
        val repository = ChatRepository(settingsManager)
        
        setContent {
            NoiosoAITheme {
                val navController = rememberNavController()
                
                // Stable ViewModel management prevents reset on layout changes
                val chatViewModel: ChatViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ChatViewModel(repository, settingsManager) as T
                        }
                    }
                )
                
                NavHost(navController = navController, startDestination = "chat") {
                    composable("chat") {
                        ChatScreen(
                            viewModel = chatViewModel,
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            settingsManager = settingsManager,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
