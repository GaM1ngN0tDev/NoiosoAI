package com.noioso.noiosoai.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noioso.noiosoai.data.local.SettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    chatRepository: com.noioso.noiosoai.data.repository.ChatRepository,
    onBackClick: () -> Unit
) {
    val ollamaIp by settingsManager.ollamaIp.collectAsStateWithLifecycle(initialValue = "")
    val ollamaModel by settingsManager.ollamaModel.collectAsStateWithLifecycle(initialValue = "")
    val systemPrompt by settingsManager.systemPrompt.collectAsStateWithLifecycle(initialValue = "")
    
    var ipInput by remember(ollamaIp) { mutableStateOf(ollamaIp) }
    var modelInput by remember(ollamaModel) { mutableStateOf(ollamaModel) }
    var systemPromptInput by remember(systemPrompt) { mutableStateOf(systemPrompt) }
    
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetchingModels by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Fetch models on start
    LaunchedEffect(ollamaIp) {
        if (ollamaIp.isNotBlank()) {
            isFetchingModels = true
            availableModels = chatRepository.getModels()
            isFetchingModels = false
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Ollama Configuration Section
            SettingsSection(
                title = "Connection", 
                icon = Icons.Rounded.Storage,
                delayIndex = 0
            ) {
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("Ollama Server URL") },
                    placeholder = { Text("e.g., http://192.168.1.10:11434") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    supportingText = {
                        Text("Include 'http://' and the port (default 11434)")
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AnimatedButton(
                    onClick = {
                        scope.launch {
                            settingsManager.saveOllamaIp(ipInput)
                        }
                    },
                    text = "Save Connection",
                    enabled = ipInput.isNotBlank() && ipInput != ollamaIp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Model Configuration Section
            SettingsSection(
                title = "AI Model", 
                icon = Icons.Rounded.Psychology,
                delayIndex = 1
            ) {
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Model Name") },
                    placeholder = { Text("e.g., llama3.2, mistral") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    supportingText = {
                        Text("The model must be already pulled in Ollama")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedButton(
                    onClick = {
                        scope.launch {
                            settingsManager.saveOllamaModel(modelInput)
                        }
                    },
                    text = "Save Model",
                    enabled = modelInput.isNotBlank() && modelInput != ollamaModel,
                    modifier = Modifier.align(Alignment.End)
                )

                if (availableModels.isNotEmpty()) {
                    Text(
                        "Available Models on Server:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableModels.forEach { model ->
                            FilterChip(
                                selected = modelInput == model,
                                onClick = { modelInput = model },
                                label = { Text(model) }
                            )
                        }
                    }
                } else if (isFetchingModels) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isFetchingModels = true
                                availableModels = chatRepository.getModels()
                                isFetchingModels = false
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Refresh Model List")
                    }
                }
            }

            // Personality Configuration Section
            SettingsSection(
                title = "Personality", 
                icon = Icons.Rounded.Tune,
                delayIndex = 2
            ) {
                OutlinedTextField(
                    value = systemPromptInput,
                    onValueChange = { systemPromptInput = it },
                    label = { Text("System Prompt") },
                    placeholder = { Text("How should the AI behave?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    minLines = 3,
                    supportingText = {
                        Text("This defines the AI's persona and rules.")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedButton(
                    onClick = {
                        scope.launch {
                            settingsManager.saveSystemPrompt(systemPromptInput)
                        }
                    },
                    text = "Save Prompt",
                    enabled = systemPromptInput != systemPrompt,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // About Section
            val uriHandler = LocalUriHandler.current
            SettingsSection(
                title = "About", 
                icon = Icons.Rounded.Info,
                delayIndex = 3
            ) {
                Text(
                    "NoiosoAI is an open-source project designed to make local AI accessible on Android.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    "Made by GaM1ngN0tDev",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/GaM1ngN0tDev/NoiosoAI") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("View on GitHub")
                }
            }
        }
    }
}

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayIndex: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayIndex * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        ) { -50 } + fadeIn(),
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.sp
                    )
                }
                content()
            }
        }
    }
}
