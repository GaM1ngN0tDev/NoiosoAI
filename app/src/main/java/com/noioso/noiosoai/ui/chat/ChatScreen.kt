package com.noioso.noiosoai.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noioso.noiosoai.data.remote.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    
    // Background animation state
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgOffset"
    )

    // Scroll logic
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.messages.lastOrNull()?.content) {
        if (uiState.messages.isNotEmpty() && uiState.isGenerating) {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (lastVisibleIndex >= uiState.messages.size - 2) {
                listState.scrollToItem(uiState.messages.size - 1)
            }
        }
    }

    // ROOT - FULL SCREEN IMMERSIVE
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Deep near-black base
    ) {
        // LAYER 1: Animated Background Glow (Flows behind everything)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(bgOffset, bgOffset / 4),
                        radius = 2800f
                    )
                )
        )

        // LAYER 2: Main Layout Architecture
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // FIXED HEADER - Anchored to status bar, never moves
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "NoiosoAI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Row {
                        TopBarButton(
                            icon = Icons.Rounded.Delete,
                            contentDescription = "Clear Chat",
                            onClick = { viewModel.clearChat() },
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TopBarButton(
                            icon = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            onClick = onSettingsClick,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // CHAT AREA - Expands to fill available space
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = uiState.messages,
                        key = { _, message -> message.id }
                    ) { index, message ->
                        val isLast = index == uiState.messages.size - 1
                        val isGenerating = isLast && uiState.isGenerating && message.role == "assistant"
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(400),
                                    placementSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                        ) {
                            ChatBubble(
                                message = message,
                                isGenerating = isGenerating,
                                modelName = uiState.model
                            )
                        }
                    }
                }

                // Floating Error Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // BOTTOM BAR AREA - Pinned to Keyboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            ) {
                ChatInputBar(
                    inputText = inputText,
                    onValueChange = { inputText = it },
                    onSendClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    onStopClick = { viewModel.stopGeneration() },
                    isGenerating = uiState.isGenerating
                )
            }
        }
    }
}

@Composable
fun TopBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.scale(scale)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp), // Expressive squircle
            color = if (isPressed) containerColor else containerColor.copy(alpha = 0.2f),
            modifier = Modifier.size(42.dp),
            tonalElevation = if (isPressed) 8.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    contentDescription = contentDescription,
                    tint = if (isPressed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, isGenerating: Boolean = false, modelName: String = "") {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isGenerating) 0.95f else 0.8f)
    }
    
    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val shape = if (isUser) {
        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 6.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                SparkleIcon(isGenerating)
                Spacer(modifier = Modifier.width(10.dp))
            }
            
            Surface(
                color = containerColor,
                contentColor = contentColor,
                shape = shape,
                shadowElevation = if (isGenerating) 4.dp else 0.dp,
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .then(
                        if (isGenerating) {
                            Modifier.background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = pulseAlpha * 0.3f),
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = pulseAlpha * 0.3f),
                                    )
                                ),
                                shape = shape
                            )
                        } else Modifier
                    )
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessHigh))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = message.content.isEmpty() && isGenerating,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "contentTransition"
                    ) { showTyping ->
                        if (showTyping) {
                            TypingIndicator()
                        } else {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 24.sp,
                                    letterSpacing = 0.25.sp
                                )
                            )
                        }
                    }
                }
            }
        }
        if (!isUser && message.content.isNotEmpty()) {
            Text(
                text = "Model: $modelName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 42.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun SparkleIcon(isAnimating: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(if (isAnimating) scale else 1f)
            .graphicsLayer {
                if (isAnimating) rotationZ = rotation
            }
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "typing")
        repeat(3) { index ->
            val delay = index * 150
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotOffset"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { translationY = yOffset }
                    .background(color = MaterialTheme.colorScheme.onSecondaryContainer, shape = CircleShape)
            )
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    isGenerating: Boolean
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 24.dp,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(32.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = inputText,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(28.dp)),
                placeholder = { Text("Ask NoiosoAI...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 6
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "buttonScale"
            )

            AnimatedContent(
                targetState = isGenerating,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.8f))
                    .togetherWith(fadeOut(animationSpec = tween(90)) + scaleOut(targetScale = 0.8f))
                },
                label = "buttonTransition"
            ) { generating ->
                if (generating) {
                    FilledIconButton(
                        onClick = onStopClick,
                        modifier = Modifier.size(52.dp).scale(buttonScale),
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = "Stop", modifier = Modifier.size(28.dp))
                    }
                } else {
                    FilledIconButton(
                        onClick = onSendClick,
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.size(52.dp).scale(buttonScale),
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        interactionSource = interactionSource
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}
