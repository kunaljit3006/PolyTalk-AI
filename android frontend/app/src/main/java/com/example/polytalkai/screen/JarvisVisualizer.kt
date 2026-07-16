package com.example.polytalkai.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@Composable
fun JarvisVisualizer(
    modifier: Modifier = Modifier,
    rmsLevelProvider: () -> Float = { 0f },
    isListening: Boolean = true,
    isSpeaking: Boolean = false,
    audioSessionId: Int? = null
) {
    var visualizerView by remember { mutableStateOf<VisualizerView?>(null) }

    // Synchronize mode transitions: "idle" | "listening" | "speaking"
    LaunchedEffect(isListening, isSpeaking) {
        visualizerView?.let { view ->
            val mode = when {
                isListening -> "listening"
                isSpeaking -> "speaking"
                else -> "idle"
            }
            view.setMode(mode)
        }
    }

    // Synchronize manual RMS updates (primarily when listening via SpeechRecognizer)
    LaunchedEffect(isListening, isSpeaking) {
        if (isListening && !isSpeaking) {
            while (true) {
                val currentRms = rmsLevelProvider()
                visualizerView?.let { view ->
                    // Map SpeechRecognizer RMS (usually -2 to 10+) to 0.0-1.0 range
                    val normalizedLevel = ((currentRms + 2f) / 12f).coerceIn(0f, 1f)
                    
                    // Generate simulated frequency bands to animate vertex ripples
                    val simulatedBands = FloatArray(16) { index ->
                        normalizedLevel * (1.0f - (index / 16f) * 0.4f) + (Math.random().toFloat() - 0.5f) * 0.1f
                    }
                    view.updateAudioLevel(normalizedLevel, simulatedBands)
                }
                delay(33) // ~30 FPS
            }
        }
    }

    // Periodically update mock levels when AI is speaking if we don't have a hardware session ID yet
    // This serves as a reliable software fallback to keep the visualizer alive.
    if (isSpeaking && audioSessionId == null) {
        LaunchedEffect(Unit) {
            var time = 0f
            while (true) {
                time += 0.15f
                // Oscillating level with voice-like noise
                val level = 0.35f + 0.4f * kotlin.math.sin(time) + (Math.random().toFloat() - 0.5f) * 0.15f
                val clampedLevel = level.coerceIn(0f, 1f)
                
                val bands = FloatArray(16) { index ->
                    clampedLevel * (1.0f - (index / 16f) * 0.4f) + (Math.random().toFloat() - 0.5f) * 0.1f
                }
                
                visualizerView?.updateAudioLevel(clampedLevel, bands)
                delay(33) // ~30 FPS
            }
        }
    }

    // Synchronize native visualizer session attachment (primarily when speaking via TTS AudioTrack)
    LaunchedEffect(audioSessionId, isSpeaking) {
        visualizerView?.let { view ->
            if (isSpeaking && audioSessionId != null) {
                view.startAudioCapture(audioSessionId)
            } else {
                view.stopAudioCapture()
            }
        }
    }

    // Manage Lifecycle (WebView pause/resume, and final destruction)
    DisposableEffect(Unit) {
        onDispose {
            visualizerView?.destroyVisualizer()
            visualizerView = null
        }
    }

    // Clip to CircleShape so the WebView's rectangular boundary is never visible.
    // The sphere's #030A10 background matches the app, and the circular mask
    // ensures a seamless, organic blend with the surrounding dark UI.
    Box(modifier = modifier.clip(CircleShape)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                VisualizerView(context).also { view ->
                    // Ensure the WebView never steals touches from the mic button above
                    view.isClickable = false
                    view.isFocusable = false
                    
                    visualizerView = view
                    // Initial set mode
                    val mode = when {
                        isListening -> "listening"
                        isSpeaking -> "speaking"
                        else -> "idle"
                    }
                    view.setMode(mode)
                }
            },
            update = {
                // Composable update trigger
            }
        )
    }
}
