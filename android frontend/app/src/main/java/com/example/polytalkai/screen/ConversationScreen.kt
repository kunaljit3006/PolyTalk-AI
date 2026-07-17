package com.example.polytalkai.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.network.PolyTalkApiClient
import com.example.polytalkai.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val translatedText: String,
    val senderType: String, // "A" or "B"
    val detectedLanguage: String,
    val targetLanguage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLive by remember { mutableStateOf(false) }
    val rmsLevel = remember { mutableFloatStateOf(0f) }

    var fromLang by remember { mutableStateOf("English") }
    var toLang by remember { mutableStateOf("Hindi") }
    
    var showFromLangDialog by remember { mutableStateOf(false) }
    var showToLangDialog by remember { mutableStateOf(false) }
    
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    var isThinking by remember { mutableStateOf(false) }

    var isTtsSpeaking by remember { mutableStateOf(false) }
    // Stop any playing TTS audio when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            PolyTalkApiClient.stopSpeaking()
        }
    }

    // Supported NLLB 18 languages
    val languages = listOf(
        "English", "Hindi", "Tamil", "Telugu", "Bengali", "Marathi",
        "Gujarati", "Kannada", "Malayalam", "Punjabi", "Odia", "Assamese",
        "French", "Spanish", "German", "Italian", "Russian", "Japanese"
    )

    // Scroll chat list to bottom when new message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Speech Recognizer Setup
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLive = true
            speechRecognizer.startListening(speechIntent)
        } else {
            Toast.makeText(context, "Microphone permission required for Live Conversation", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to submit a sentence
    fun processSpeechSubmission(rawText: String) {
        if (rawText.isBlank() || isThinking) {
            if (isLive) speechRecognizer.startListening(speechIntent)
            return
        }
        
        isThinking = true
        coroutineScope.launch {
            // Auto detect language
            val detected = detectLanguage(rawText, fromLang, toLang)
            val target = if (detected == fromLang) toLang else fromLang
            val sender = if (detected == fromLang) "A" else "B"

            // Call API
            val result = PolyTalkApiClient.translate(rawText, detected, target)
            isThinking = false

            if (result.isSuccess) {
                val translated = result.getOrThrow()
                chatMessages.add(
                    ChatMessage(
                        text = rawText,
                        translatedText = translated,
                        senderType = sender,
                        detectedLanguage = detected,
                        targetLanguage = target
                    )
                )

                // Speak translation out loud automatically
                PolyTalkApiClient.speak(
                    text = translated,
                    fromLang = target,
                    onStart = { isTtsSpeaking = true },
                    onDone = { isTtsSpeaking = false }
                )
            } else {
                Toast.makeText(context, "Translation failed. Check backend connection.", Toast.LENGTH_SHORT).show()
            }
            // Resume listening loop automatically
            if (isLive) speechRecognizer.startListening(speechIntent)
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) { rmsLevel.floatValue = rmsdB }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { rmsLevel.floatValue = 0f }
            override fun onError(error: Int) {
                rmsLevel.floatValue = 0f
                if (isLive) {
                    // Slight delay before restarting to prevent rapid looping
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(500)
                        if (isLive) speechRecognizer.startListening(speechIntent)
                    }
                }
            }
            override fun onResults(results: Bundle?) {
                rmsLevel.floatValue = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processSpeechSubmission(matches[0])
                } else {
                    if (isLive) speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(recognitionListener)
        onDispose { speechRecognizer.destroy() }
    }

    fun toggleLive() {
        if (!isLive) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                isLive = true
                speechRecognizer.startListening(speechIntent)
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            isLive = false
            rmsLevel.floatValue = 0f
            speechRecognizer.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceColor)
                    .border(1.dp, GlassBorderColor, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Live Conversation",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextColor
            )
        }

        // Language Pickers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .glassmorphic(cornerRadius = 16)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceColor2)
                    .clickable { showFromLangDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = fromLang,
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextColor
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(Grad1, Grad2)),
                        shape = CircleShape
                    )
                    .clickable {
                        val temp = fromLang
                        fromLang = toLang
                        toLang = temp
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceColor2)
                    .clickable { showToLangDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = toLang,
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat Bubble area (Visible when live mode is active or has messages)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            if (chatMessages.isEmpty()) {
                // Empty instruction state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Listen",
                        tint = GlassBorderColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Press the mic below to start.",
                        fontFamily = SatoshiFontFamily,
                        color = TextMutedColor,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUserA = (msg.senderType == "A")
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = if (isUserA) Arrangement.Start else Arrangement.End
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .glassmorphic(cornerRadius = 16)
                                    .border(
                                        width = 1.dp,
                                        color = if (isUserA) PrimaryColor.copy(alpha = 0.5f) else AccentColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontFamily = SatoshiFontFamily,
                                    fontSize = 16.sp,
                                    color = TextColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = GlassBorderColor, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg.translatedText,
                                        fontFamily = SatoshiFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (isUserA) PrimaryColor else AccentColor
                                    )
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Read out",
                                        tint = TextMutedColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                PolyTalkApiClient.speak(
                                                    text = msg.translatedText,
                                                    fromLang = msg.targetLanguage,
                                                    onStart = { isTtsSpeaking = true },
                                                    onDone = { isTtsSpeaking = false }
                                                )
                                            }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${msg.detectedLanguage} → ${msg.targetLanguage}",
                                    fontSize = 11.sp,
                                    fontFamily = SatoshiFontFamily,
                                    color = TextMutedColor
                                )
                            }
                        }
                    }
                    if (isThinking) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Auto-translating speech...",
                                    fontFamily = SatoshiFontFamily,
                                    fontSize = 13.sp,
                                    color = PrimaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Animated Jarvis Visualizer & Mic Listening Action
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Standalone AI Visualizer Orb
            JarvisVisualizer(
                modifier = Modifier.size(240.dp),
                rmsLevelProvider = { rmsLevel.floatValue },
                isListening = isLive && !isTtsSpeaking,
                isSpeaking = isTtsSpeaking
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Conversation listen button
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (isLive) {
                            Brush.linearGradient(listOf(DangerColor, Grad1))
                        } else {
                            Brush.linearGradient(listOf(AccentColor, PrimaryColor))
                        }
                    )
                    .clickable { toggleLive() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .background(BgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Live Micro",
                        tint = if (isLive) DangerColor else PrimaryColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isLive) "AUTO LISTENING ACTIVE" else "TAP TO START LIVE AUTO-CONVERSATION",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isLive) DangerColor else TextMutedColor,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Language selector dialog A
    if (showFromLangDialog) {
        LanguageSelectionDialog(
            title = "Select Language A",
            currentLanguage = fromLang,
            languages = languages,
            onDismiss = { showFromLangDialog = false },
            onSelect = { fromLang = it }
        )
    }

    // Language selector dialog B
    if (showToLangDialog) {
        LanguageSelectionDialog(
            title = "Select Language B",
            currentLanguage = toLang,
            languages = languages,
            onDismiss = { showToLangDialog = false },
            onSelect = { toLang = it }
        )
    }
}

// Language auto-detection engine based on script character ranges
private fun detectLanguage(text: String, langA: String, langB: String): String {
    val containsDevanagari = text.any { it in '\u0900'..'\u097F' }
    val containsTamil = text.any { it in '\u0B80'..'\u0BFF' }
    val containsTelugu = text.any { it in '\u0C00'..'\u0C7F' }
    val containsBengali = text.any { it in '\u0980'..'\u09FF' } // Bengali / Assamese
    val containsGujarati = text.any { it in '\u0A80'..'\u0AFF' }
    val containsKannada = text.any { it in '\u0C80'..'\u0CFF' }
    val containsMalayalam = text.any { it in '\u0D00'..'\u0D7F' }
    val containsOdia = text.any { it in '\u0B00'..'\u0B7F' }
    val containsJapanese = text.any { it in '\u3040'..'\u30FF' || it in '\u4E00'..'\u9FFF' }
    val containsCyrillic = text.any { it in '\u0400'..'\u04FF' } // Russian

    fun matchesScript(langName: String): Boolean {
        return when (langName) {
            "Hindi", "Marathi" -> containsDevanagari
            "Tamil" -> containsTamil
            "Telugu" -> containsTelugu
            "Bengali", "Assamese" -> containsBengali
            "Gujarati" -> containsGujarati
            "Kannada" -> containsKannada
            "Malayalam" -> containsMalayalam
            "Odia" -> containsOdia
            "Japanese" -> containsJapanese
            "Russian" -> containsCyrillic
            else -> false
        }
    }

    return when {
        matchesScript(langA) -> langA
        matchesScript(langB) -> langB
        // Standard Latin alphabet script (English, Spanish, French, Italian, German, etc.)
        langA == "English" && text.all { it.code < 128 || it.isWhitespace() || it.isDigit() } -> langA
        langB == "English" && text.all { it.code < 128 || it.isWhitespace() || it.isDigit() } -> langB
        else -> langA // Default fallback
    }
}


