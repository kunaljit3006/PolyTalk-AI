package com.example.polytalkai.screen

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.polytalkai.glassmorphic
import com.example.polytalkai.network.PolyTalkApiClient
import com.example.polytalkai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CameraOcrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var isScanning by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var isShutterFlashing by remember { mutableStateOf(false) }

    var ocrSourceText by remember { mutableStateOf("") }
    var ocrTranslatedText by remember { mutableStateOf("") }
    var typedOcrTranslatedText by remember { mutableStateOf("") }

    var targetLang by remember { mutableStateOf("Spanish") }
    var showLangDialog by remember { mutableStateOf(false) }

    var containerHeightPx by remember { mutableStateOf(0) }
    
    // Stop any playing TTS audio when navigating away from this screen
    DisposableEffect(Unit) {
        onDispose {
            PolyTalkApiClient.stopSpeaking()
        }
    }

    // Laser Animation when scanning mode is active
    val infiniteTransition = rememberInfiniteTransition(label = "LaserSweep")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    // Typewriter effect logic
    LaunchedEffect(ocrTranslatedText) {
        if (ocrTranslatedText.isNotEmpty()) {
            typedOcrTranslatedText = ""
            for (i in ocrTranslatedText.indices) {
                typedOcrTranslatedText += ocrTranslatedText[i]
                delay(30)
            }
        }
    }

    // Supported NLLB 18 languages
    val languages = listOf(
        "English", "Hindi", "Tamil", "Telugu", "Bengali", "Marathi",
        "Gujarati", "Kannada", "Malayalam", "Punjabi", "Odia", "Assamese",
        "French", "Spanish", "German", "Italian", "Russian", "Japanese"
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Camera OCR",
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = TextColor
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Target Language Badge (Clickable)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryColor.copy(alpha = 0.15f))
                        .border(1.dp, PrimaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { showLangDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = targetLang,
                        fontFamily = SatoshiFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Viewfinder / Results Layout Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!showResults) {
                    if (hasCameraPermission) {
                        CameraPreviewView(modifier = Modifier.fillMaxSize(), isFrozen = isScanning || showResults)
                    } else {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan",
                                tint = TextMutedColor,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Camera permission required.",
                                fontFamily = SatoshiFontFamily,
                                color = TextMutedColor,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Text("Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Radar Scan lines overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridCount = 5
                        val cellWidth = size.width / gridCount
                        val cellHeight = size.height / gridCount
                        for (i in 1 until gridCount) {
                            drawLine(
                                color = GlassBorderColor.copy(alpha = 0.1f),
                                start = Offset(i * cellWidth, 0f),
                                end = Offset(i * cellWidth, size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = GlassBorderColor.copy(alpha = 0.1f),
                                start = Offset(0f, i * cellHeight),
                                end = Offset(size.width, i * cellHeight),
                                strokeWidth = 1f
                            )
                        }
                    }

                    if (!isScanning) {
                        // Sweeping Laser animation overlay (Idle Mode)
                        val laserHeightDp = with(LocalContext.current.resources.displayMetrics.density) { (laserOffset * 1000).dp }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = laserHeightDp - 200.dp)
                                .background(Brush.horizontalGradient(listOf(Grad1, Grad2, Grad1)))
                                .border(1.dp, PrimaryColor)
                        )
                    }
                }

                // Shutter flash effect
                if (isShutterFlashing) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.85f)))
                }

                // OCR Extraction Progress
                if (isScanning) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
                        val scanPosition by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
                            label = "scanPos"
                        )
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val y = size.height * scanPosition
                            // Draw a glowing horizontal laser line
                            drawLine(
                                color = PrimaryColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            // Draw a glowing gradient fading up from the line
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, PrimaryColor.copy(alpha = 0.5f)),
                                    startY = y - 120f,
                                    endY = y
                                ),
                                topLeft = Offset(0f, y - 120f),
                                size = Size(size.width, 120f)
                            )
                        }
                    }
                }

                // Gemini-style Chat View Results
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showResults,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // User Message (Original Text)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .glassmorphic(cornerRadius = 16)
                                    .border(1.dp, GlassBorderColor, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Scanned Original:",
                                    fontFamily = SatoshiFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextMutedColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ocrSourceText,
                                    fontFamily = SatoshiFontFamily,
                                    fontSize = 15.sp,
                                    color = TextColor,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // AI Assistant Message (Translation)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceColor)
                                    .border(1.dp, PrimaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PolyTalk AI ($targetLang)",
                                        fontFamily = SatoshiFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PrimaryColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = typedOcrTranslatedText,
                                    fontFamily = SatoshiFontFamily,
                                    fontSize = 16.sp,
                                    color = TextColor,
                                    lineHeight = 24.sp
                                )

                                if (typedOcrTranslatedText.length == ocrTranslatedText.length && ocrTranslatedText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = GlassBorderColor, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = { 
                                            clipboardManager.setText(AnnotatedString(ocrTranslatedText))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMutedColor)
                                        }
                                        IconButton(onClick = { 
                                            PolyTalkApiClient.speak(ocrTranslatedText, targetLang)
                                        }) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = TextMutedColor)
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Buttons
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (!showResults) {
                    Button(
                        onClick = {
                            isShutterFlashing = true
                            coroutineScope.launch {
                                delay(120)
                                isShutterFlashing = false
                                isScanning = true
                                
                                // Simulate extraction delay then API call
                                val source = "PolyTalk connects the world through intelligent, seamless translations."
                                ocrSourceText = source
                                
                                val res = PolyTalkApiClient.translate(source, "English", targetLang)
                                isScanning = false
                                showResults = true
                                if (res.isSuccess) {
                                    ocrTranslatedText = res.getOrThrow()
                                } else {
                                    ocrTranslatedText = "Error communicating with translation server."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(76.dp)
                            .border(4.dp, TextColor, CircleShape)
                            .padding(6.dp)
                            .background(brush = Brush.horizontalGradient(listOf(Grad1, Grad2)), shape = CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { showLangDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor2),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(text = "Change Target", fontFamily = SatoshiFontFamily, fontWeight = FontWeight.Bold, color = TextColor)
                        }

                        Button(
                            onClick = {
                                showResults = false
                                ocrSourceText = ""
                                ocrTranslatedText = ""
                                typedOcrTranslatedText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(text = "Rescan", fontFamily = SatoshiFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    if (showLangDialog) {
        LanguageSelectionDialog(
            title = "Select Target Language",
            currentLanguage = targetLang,
            languages = languages,
            onDismiss = { showLangDialog = false },
            onSelect = { 
                targetLang = it
                if (ocrSourceText.isNotEmpty()) {
                    // Retranslate if changing target
                    ocrTranslatedText = ""
                    typedOcrTranslatedText = ""
                    coroutineScope.launch {
                        val res = PolyTalkApiClient.translate(ocrSourceText, "English", it)
                        if (res.isSuccess) ocrTranslatedText = res.getOrThrow()
                    }
                }
            }
        )
    }
}

// CameraX Preview Setup
@Composable
fun CameraPreviewView(modifier: Modifier = Modifier, isFrozen: Boolean = false) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
    var frozenBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // When isFrozen becomes true, capture the current frame
    LaunchedEffect(isFrozen) {
        if (isFrozen) {
            frozenBitmap = previewView?.bitmap
        } else {
            frozenBitmap = null
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                androidx.camera.view.PreviewView(context).apply {
                    previewView = this
                    val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }
                        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay the frozen frame if captured
        frozenBitmap?.let { bmp ->
            androidx.compose.foundation.Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Frozen Camera Frame",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}


