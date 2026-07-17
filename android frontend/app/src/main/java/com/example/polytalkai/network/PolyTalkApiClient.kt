package com.example.polytalkai.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.media.MediaPlayer
import java.net.URLEncoder

object PolyTalkApiClient {
    // URL of Hugging Face Space API
    var baseUrl: String = "https://heykunal123-polytalk-ai-backend.hf.space"

    private var mediaPlayer: MediaPlayer? = null

    fun speak(
        text: String,
        fromLang: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        val srcCode = languageMap[fromLang] ?: "eng_Latn"
        speakWithCode(text, srcCode, onStart, onDone)
    }

    fun speakWithCode(
        text: String,
        nllbLangCode: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        try {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.release()
            }

            val encodedText = URLEncoder.encode(text, "UTF-8")
            val audioUrl = "${baseUrl}/tts?text=$encodedText&lang=$nllbLangCode"

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    it.start()
                    onStart()
                }
                setOnCompletionListener {
                    onDone()
                    it.release()
                    if (mediaPlayer == this) {
                        mediaPlayer = null
                    }
                }
                setOnErrorListener { _, _, _ ->
                    onDone()
                    release()
                    if (mediaPlayer == this) {
                        mediaPlayer = null
                    }
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onDone()
        }
    }

    fun stopSpeaking() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Maps clean language names to NLLB codes
    val languageMap = mapOf(
        "English" to "eng_Latn",
        "Hindi" to "hin_Deva",
        "Tamil" to "tam_Taml",
        "Telugu" to "tel_Telu",
        "Bengali" to "ben_Beng",
        "Marathi" to "mar_Deva",
        "Gujarati" to "guj_Gujr",
        "Kannada" to "kan_Knda",
        "Malayalam" to "mal_Mlym",
        "Punjabi" to "pan_Guru",
        "Odia" to "ory_Orya",
        "Assamese" to "asm_Beng",
        "French" to "fra_Latn",
        "Spanish" to "spa_Latn",
        "German" to "deu_Latn",
        "Italian" to "ita_Latn",
        "Russian" to "rus_Cyrl",
        "Japanese" to "jpn_Jpan"
    )

    suspend fun translate(text: String, fromLang: String, toLang: String): Result<String> = withContext(Dispatchers.IO) {
        val srcCode = languageMap[fromLang] ?: "eng_Latn"
        val tgtCode = languageMap[toLang] ?: "hin_Deva"

        try {
            // 1. Try production HF Space API first
            val prodResult = makeRequest(baseUrl, text, srcCode, tgtCode, readTimeoutMs = 60000)
            if (prodResult.isSuccess) {
                prodResult
            } else {
                // 2. Fallback to Localhost for development (Android emulator maps 10.0.2.2 to localhost)
                val fallbackResult = makeRequest("http://10.0.2.2:8000", text, srcCode, tgtCode, readTimeoutMs = 15000)
                if (fallbackResult.isSuccess) {
                    fallbackResult
                } else {
                    // If fallback also fails, return the production error (which is the real issue)
                    prodResult
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun makeRequest(urlStr: String, text: String, srcCode: String, tgtCode: String, readTimeoutMs: Int): Result<String> {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$urlStr/translate")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = readTimeoutMs

            val jsonInputString = JSONObject().apply {
                put("text", text)
                put("src_lang", srcCode)
                put("tgt_lang", tgtCode)
            }.toString()

            connection.outputStream.use { os ->
                val input = jsonInputString.toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }

            val code = connection.responseCode
            return if (code == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                Result.success(jsonObject.getString("translated_text"))
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Error code $code"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
