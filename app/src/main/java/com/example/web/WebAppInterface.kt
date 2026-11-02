package com.example.web

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.model.AssistantId
import com.example.data.repository.DTFGRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.Locale

class WebAppInterface(
    private val activity: Activity,
    private val repository: DTFGRepository,
    private val webViewProvider: () -> WebView?
) {
    private val TAG = "WebAppInterface"
    private var speechRecognizer: SpeechRecognizer? = null

    @JavascriptInterface
    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    @JavascriptInterface
    fun sendMessage(assistantKey: String, prompt: String): String {
        val assistantId = AssistantId.fromKey(assistantKey)
        val result = runBlocking {
            repository.sendMessage(assistantId, prompt, emptyList())
        }

        val json = JSONObject()
        if (result.isSuccess) {
            val msg = result.getOrNull()
            json.put("content", msg?.content ?: "")
            json.put("engineNote", msg?.engineNote ?: "DTFG World Hub Engine")
        } else {
            json.put("error", result.exceptionOrNull()?.localizedMessage ?: "Failed to generate response")
        }
        return json.toString()
    }

    @JavascriptInterface
    fun startNativeDictation() {
        activity.runOnUiThread {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 2001)
                notifyWeb("window.onVoiceDictationPermissionNeeded?.()")
                return@runOnUiThread
            }

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        notifyWeb("window.onVoiceDictationStarted?.()")
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        notifyWeb("window.onVoiceDictationStopped?.()")
                    }

                    override fun onError(error: Int) {
                        Log.w(TAG, "Speech recognition error: $error")
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap mic to try again."
                            else -> "Voice recognition stopped (Code $error)"
                        }
                        val escaped = JSONObject.quote(errorMsg)
                        notifyWeb("window.onVoiceDictationError?.($escaped)")
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            val escaped = JSONObject.quote(text)
                            notifyWeb("window.onVoiceDictationResult?.($escaped)")
                        }
                        notifyWeb("window.onVoiceDictationStopped?.()")
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotBlank()) {
                            val escaped = JSONObject.quote(partial)
                            notifyWeb("window.onVoiceDictationPartial?.($escaped)")
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognition", e)
                notifyWeb("window.onVoiceDictationError?.(${JSONObject.quote(e.localizedMessage ?: "Voice recognition unavailable")})")
            }
        }
    }

    @JavascriptInterface
    fun stopNativeDictation() {
        activity.runOnUiThread {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop speech recognition", e)
            }
        }
    }

    @JavascriptInterface
    fun downloadJsonFile(filename: String, jsonContent: String): Boolean {
        return try {
            activity.runOnUiThread {
                try {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TITLE, filename)
                        putExtra(Intent.EXTRA_SUBJECT, filename)
                        putExtra(Intent.EXTRA_TEXT, jsonContent)
                        type = "application/json"
                    }
                    activity.startActivity(Intent.createChooser(sendIntent, "Download $filename"))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch download/export intent", e)
                }
            }
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to download json file", e)
            false
        }
    }

    private fun notifyWeb(script: String) {
        activity.runOnUiThread {
            webViewProvider()?.evaluateJavascript(script, null)
        }
    }
}
