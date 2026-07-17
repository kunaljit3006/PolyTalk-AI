package com.example.polytalkai.screen

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.network.PolyTalkApiClient
import com.example.polytalkai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TextTranslationScreen(
    onBack: () -> Unit,
    onTranslate: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var sourceText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var typedTranslatedText by remember { mutableStateOf("") }
    var fromLang by remember { mutableStateOf("English") }
    var toLang by remember { mutableStateOf("Hindi") }
    
    var showOutput by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingStep by remember { mutableStateOf(0) }

    var showFromLangDialog by remember { mutableStateOf(false) }
    var showToLangDialog by remember { mutableStateOf(false) }

    // Stop any playing TTS audio when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            PolyTalkApiClient.stopSpeaking()
        }
    }

    // NLLB supported 18 languages
    val languages = listOf(
        "English", "Hindi", "Tamil", "Telugu", "Bengali", "Marathi",
        "Gujarati", "Kannada", "Malayalam", "Punjabi", "Odia", "Assamese",
        "French", "Spanish", "German", "Italian", "Russian", "Japanese"
    )

    val loadingSteps = listOf(
        "Connecting to PolyTalk AI Server...",
        "Analyzing text script family...",
        "Applying fine-tuned LoRA weights...",
        "Running NLLB-200 1.3B inference (hf-cpu)...",
        "Polishing output context & grammar..."
    )

    // Animated rotation for the loading spinner
    val infiniteTransition = rememberInfiniteTransition(label = "Spinner")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotate"
    )

    // Typing effect when translatedText changes
    LaunchedEffect(translatedText) {
        if (translatedText.isNotEmpty()) {
            typedTranslatedText = ""
            // Typewriter effect speed: 15ms per character
            translatedText.forEach { char ->
                typedTranslatedText += char
                delay(12)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
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
                text = "Text Translation",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = TextColor
            )
        }

        // Language selectors row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .glassmorphic(cornerRadius = 16)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source Language Picker
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

            // Swap Button
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
                        if (translatedText.isNotEmpty()) {
                            val tempText = sourceText
                            sourceText = translatedText
                            translatedText = tempText
                        }
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

            // Target Language Picker
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

        Spacer(modifier = Modifier.height(20.dp))

        // Input card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .glassmorphic(cornerRadius = 16)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Source Text ($fromLang)",
                    fontFamily = SatoshiFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TextMutedColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    if (sourceText.isEmpty()) {
                        Text(
                            text = "Enter text to translate...",
                            fontFamily = SatoshiFontFamily,
                            fontSize = 18.sp,
                            color = TextMutedColor
                        )
                    }
                    BasicTextField(
                        value = sourceText,
                        onValueChange = {
                            sourceText = it
                            if (it.isEmpty()) showOutput = false
                        },
                        textStyle = TextStyle(
                            color = TextColor,
                            fontFamily = SatoshiFontFamily,
                            fontSize = 18.sp
                        ),
                        cursorBrush = SolidColor(PrimaryColor),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Translate button
        Button(
            onClick = {
                if (sourceText.isNotEmpty() && !isLoading) {
                    isLoading = true
                    showOutput = false
                    loadingStep = 0

                    coroutineScope.launch {
                        // Cycle loading messages during HTTP latency
                        val stepJob = launch {
                            while (true) {
                                delay(1500)
                                if (loadingStep < loadingSteps.size - 1) {
                                    loadingStep++
                                }
                            }
                        }

                        val result = PolyTalkApiClient.translate(sourceText, fromLang, toLang)
                        stepJob.cancel()
                        isLoading = false

                        if (result.isSuccess) {
                            translatedText = result.getOrThrow()
                            showOutput = true
                            onTranslate(sourceText, translatedText, fromLang, toLang)
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "Unknown Error"
                            translatedText = "[API Fallback] Translate Failed ($err)"
                            showOutput = true
                        }
                    }
                }
            },
            enabled = sourceText.isNotEmpty() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(54.dp)
                .background(
                    brush = if (sourceText.isNotEmpty() && !isLoading) {
                        Brush.horizontalGradient(listOf(Grad1, Grad2))
                    } else {
                        SolidColor(SurfaceColor2)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = if (isLoading) "TRANSLATING..." else "TRANSLATE",
                fontFamily = SatoshiFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (sourceText.isNotEmpty() && !isLoading) Color.White else TextMutedColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Loading Card Panel
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .glassmorphic(backgroundColor = SurfaceColor.copy(alpha = 0.5f), cornerRadius = 16)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Futuristic Neon Progress Ring
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .rotate(rotationAngle)
                            .border(
                                width = 4.dp,
                                brush = Brush.sweepGradient(listOf(Grad1, Grad2, Grad1)),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    // Step Text Loader
                    Text(
                        text = loadingSteps[loadingStep],
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PrimaryColor,
                        modifier = Modifier.animateContentSize()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This takes a few seconds on CPU servers",
                        fontFamily = SatoshiFontFamily,
                        fontSize = 12.sp,
                        color = TextMutedColor
                    )
                }
            }
        }

        // Output card (Show after translation)
        AnimatedVisibility(
            visible = showOutput && !isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .glassmorphic(backgroundColor = Color(0x330076FF), cornerRadius = 16)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Translation ($toLang)",
                            fontFamily = SatoshiFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextMutedColor,
                            letterSpacing = 0.5.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // TTS Speaker
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "TTS",
                                tint = PrimaryColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        PolyTalkApiClient.speak(translatedText, toLang)
                                    }
                            )
                            // Copy button
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = PrimaryColor,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(translatedText))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Typer effect results display
                    Text(
                        text = typedTranslatedText,
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        color = TextColor,
                        lineHeight = 28.sp
                    )
                }
            }
        }
    }

    // Source Language Selection Dialog
    if (showFromLangDialog) {
        LanguageSelectionDialog(
            title = "Select Source Language",
            currentLanguage = fromLang,
            languages = languages,
            onDismiss = { showFromLangDialog = false },
            onSelect = { fromLang = it }
        )
    }

    // Target Language Selection Dialog
    if (showToLangDialog) {
        LanguageSelectionDialog(
            title = "Select Target Language",
            currentLanguage = toLang,
            languages = languages,
            onDismiss = { showToLangDialog = false },
            onSelect = { toLang = it }
        )
    }
}


