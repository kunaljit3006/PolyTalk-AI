package com.example.polytalkai.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.polytalkai.network.PolyTalkApiClient
import com.example.polytalkai.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTranslationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isListening by remember { mutableStateOf(false) }
    val rmsLevel = remember { mutableFloatStateOf(0f) }

    var fromLang by remember { mutableStateOf("English") }
    var toLang by remember { mutableStateOf("Spanish") }
    var showFromLangDialog by remember { mutableStateOf(false) }
    var showToLangDialog by remember { mutableStateOf(false) }

    var recognizedText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    // Stop any playing TTS audio when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            PolyTalkApiClient.stopSpeaking()
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

    // Speech Listener
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                rmsLevel.floatValue = rmsdB
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                rmsLevel.floatValue = 0f
            }
            override fun onError(error: Int) {
                isListening = false
                rmsLevel.floatValue = 0f
                Toast.makeText(context, "Speech Recognition Error: $error", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    recognizedText = text
                    isTranslating = true

                    coroutineScope.launch {
                        val result = PolyTalkApiClient.translate(text, fromLang, toLang)
                        if (result.isSuccess) {
                            translatedText = result.getOrThrow()
                            isTranslating = false
                            
                            PolyTalkApiClient.speak(
                                text = translatedText,
                                fromLang = toLang,
                                onStart = { isSpeaking = true },
                                onDone = { isSpeaking = false }
                            )
                        } else {
                            translatedText = "Translation failed."
                            isTranslating = false
                        }
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(recognitionListener)
        onDispose { speechRecognizer.destroy() }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            speechRecognizer.startListening(speechIntent)
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            isListening = true
            speechRecognizer.startListening(speechIntent)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopListening() {
        isListening = false
        rmsLevel.floatValue = 0f
        speechRecognizer.stopListening()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sticky Header
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
                text = "Voice Translation",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextColor
            )
        }

        // Language Selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lang A
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(14.dp))
                    .clickable { showFromLangDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(text = fromLang, color = TextColor, fontFamily = SatoshiFontFamily, fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = {
                    val temp = fromLang
                    fromLang = toLang
                    toLang = temp
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Swap", tint = PrimaryColor)
            }
            
            // Lang B
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(14.dp))
                    .clickable { showToLangDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(text = toLang, color = TextColor, fontFamily = SatoshiFontFamily, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Standalone AI Visualizer Orb
        JarvisVisualizer(
            modifier = Modifier.size(240.dp),
            rmsLevelProvider = { rmsLevel.floatValue },
            isListening = isListening,
            isSpeaking = isSpeaking
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // Transcription Results
        AnimatedVisibility(visible = recognizedText.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = recognizedText,
                    fontFamily = SatoshiFontFamily,
                    fontSize = 18.sp,
                    color = TextColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (isTranslating) {
                    CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (translatedText.isNotEmpty()) {
                    Text(
                        text = translatedText,
                        fontFamily = SatoshiFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // State indicator label
            Text(
                text = if (isListening) "LISTENING..." else "TAP MIC TO SPEAK",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isListening) PrimaryColor else TextMutedColor,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Microphone trigger
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        brush = if (isListening) {
                            Brush.horizontalGradient(listOf(DangerColor, Grad1))
                        } else {
                            Brush.horizontalGradient(listOf(Grad1, Grad2))
                        },
                        shape = CircleShape
                    )
                    .clickable {
                        if (isListening) stopListening() else startListening()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        } // Close inner Column
    } // Close root Column


    val languages = listOf(
        "English", "Hindi", "Tamil", "Telugu", "Bengali", "Marathi",
        "Gujarati", "Kannada", "Malayalam", "Punjabi", "Odia", "Assamese",
        "French", "Spanish", "German", "Italian", "Russian", "Japanese"
    )

    if (showFromLangDialog) {
        LanguageSelectionDialog(
            title = "Select Language A",
            currentLanguage = fromLang,
            languages = languages,
            onDismiss = { showFromLangDialog = false },
            onSelect = { fromLang = it }
        )
    }

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


