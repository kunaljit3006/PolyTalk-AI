package com.example.polytalkai.screen

import android.content.Context
import android.graphics.Color
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray

class VisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val TAG = "VisualizerView"
    private var nativeVisualizer: Visualizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isJsLoaded = false
    private var pendingMode: String? = null

    init {
        // Match the WebView background to the app's exact dark background (#030A10).
        // This guarantees seamless blending on ALL Android devices without relying
        // on WebView alpha compositing, which is unreliable across manufacturers.
        setBackgroundColor(Color.parseColor("#030A10"))

        // WebSettings configuration
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        // Force hardware acceleration to guarantee smooth 60fps WebGL compositing inside Jetpack Compose
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Re-apply matching background after page load
                view?.setBackgroundColor(Color.parseColor("#030A10"))
                isJsLoaded = true
                pendingMode?.let { evaluateJs("window.setVisualizerMode('$it')") }
                Log.d(TAG, "Visualizer HTML page loaded")
            }
        }

        webChromeClient = WebChromeClient()

        // Load visualizer from assets with the transparent/android flag as a hash to avoid file not found errors
        loadUrl("file:///android_asset/visualizer.html#android=true")
    }

    /**
     * Sets the visualizer mode/state: "idle" | "listening" | "speaking"
     */
    fun setMode(mode: String) {
        if (!isJsLoaded) {
            pendingMode = mode
        }
        evaluateJs("window.setVisualizerMode('$mode')")
    }

    /**
     * Passes audio level data to the JS bridge. Optimized to prevent memory allocation / GC stutter.
     */
    fun updateAudioLevel(level: Float, freqBands: FloatArray = FloatArray(0)) {
        // We only need to pass the level since the new Aura shader does not use 16 freq bands.
        // This avoids allocating and serializing a JSONArray 30x a second, eliminating GC stutter.
        evaluateJs("window.updateAudioLevel($level)")
    }

    /**
     * Pauses the Three.js render loop to conserve battery
     */
    fun pauseVisualizer() {
        evaluateJs("window.pauseVisualizer()")
    }

    /**
     * Resumes the Three.js render loop
     */
    fun resumeVisualizer() {
        evaluateJs("window.resumeVisualizer()")
    }

    /**
     * Performs a full cleanup of WebView and WebGL resources to prevent GPU context leaks
     */
    fun destroyVisualizer() {
        stopAudioCapture()
        evaluateJs("window.disposeVisualizer()")
        destroy()
    }

    /**
     * Attaches android.media.audiofx.Visualizer to a specific audio session ID.
     * Implements a fallback to session ID 0 (global output mix) if attachment fails.
     */
    fun startAudioCapture(audioSessionId: Int) {
        stopAudioCapture()
        
        try {
            Log.d(TAG, "Attempting to attach Visualizer to audio session: $audioSessionId")
            nativeVisualizer = Visualizer(audioSessionId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach to specific session $audioSessionId. Falling back to session 0 (Global Mix).", e)
            try {
                nativeVisualizer = Visualizer(0)
            } catch (eFallback: Exception) {
                Log.e(TAG, "Failed to attach to global audio session 0.", eFallback)
                return
            }
        }

        nativeVisualizer?.let { viz ->
            try {
                viz.captureSize = Visualizer.getCaptureSizeRange()[1]
                
                viz.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (waveform == null) return
                        
                        // Calculate overall volume level (RMS)
                        val level = calculateRms(waveform)
                        
                        mainHandler.post {
                            updateAudioLevel(level)
                        }
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft == null) return
                        
                        // Calculate frequency bands and overall peak level
                        val bands = processFft(fft)
                        val level = bands.maxOrNull() ?: 0.0f
                        
                        mainHandler.post {
                            updateAudioLevel(level, bands)
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true)

                viz.enabled = true
                Log.d(TAG, "Native visualizer capture enabled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring Visualizer", e)
            }
        }
    }

    /**
     * Disables and releases the native Android Visualizer instance
     */
    fun stopAudioCapture() {
        try {
            nativeVisualizer?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing Visualizer", e)
        }
        nativeVisualizer = null
    }

    private fun calculateRms(waveform: ByteArray): Float {
        var sum = 0.0
        for (i in waveform.indices) {
            // Waveform contains unsigned 8-bit values (range 0 to 255, center is 128)
            val value = (waveform[i].toInt() and 0xFF) - 128
            sum += value * value
        }
        val mean = sum / waveform.size
        val rms = Math.sqrt(mean)
        // Scale to 0.0 - 1.0 range (rms peak value is 128)
        return (rms / 128.0).toFloat().coerceIn(0.0f, 1.0f)
    }

    private fun processFft(fft: ByteArray): FloatArray {
        // FFT array contains real and imaginary parts
        val numMagnitudes = fft.size / 2
        val magnitudes = FloatArray(numMagnitudes)
        
        for (i in 0 until numMagnitudes) {
            val r = fft[i * 2].toInt()
            val im = fft[i * 2 + 1].toInt()
            val mag = Math.sqrt((r * r + im * im).toDouble()).toFloat()
            magnitudes[i] = (mag / 128.0f).coerceIn(0.0f, 1.0f)
        }

        // Downsample FFT resolution to 16 bands for Three.js shader performance
        val bands = FloatArray(16)
        if (numMagnitudes > 0) {
            val binSize = numMagnitudes / 16
            for (i in 0 until 16) {
                var sum = 0.0f
                var count = 0
                val start = i * binSize
                val end = if (i == 15) numMagnitudes else (i + 1) * binSize
                for (j in start until end) {
                    sum += magnitudes[j]
                    count++
                }
                bands[i] = if (count > 0) sum / count else 0.0f
            }
        }
        return bands
    }

    private fun evaluateJs(script: String) {
        if (!isJsLoaded) return
        evaluateJavascript(script, null)
    }
}
