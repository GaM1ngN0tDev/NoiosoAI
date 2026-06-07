package com.noioso.noiosoai.data.repository

import android.util.Log
import com.noioso.noiosoai.data.local.SettingsManager
import com.noioso.noiosoai.data.remote.ChatRequest
import com.noioso.noiosoai.data.remote.ChatResponse
import com.noioso.noiosoai.data.remote.Message
import com.noioso.noiosoai.data.remote.OllamaApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ChatRepository handles all communication with the local Ollama instance.
 * Security Note: This implementation is "Local-Only". It does not send data to any 3rd party
 * servers or telemetry services. All data stays on the device and the user-defined IP.
 */
class ChatRepository(private val settingsManager: SettingsManager) {

    private val TAG = "ChatRepository"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // OKHttpClient configured for local traffic. 
    // BODY logging is removed to prevent private chat data from appearing in system logs.
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            // Changed to HEADERS only to protect message privacy in logs
            level = HttpLoggingInterceptor.Level.HEADERS 
        })
        .build()

    private suspend fun getRetrofit(): Retrofit {
        var baseUrl = settingsManager.ollamaIp.first().trim()
        
        // Remove trailing slashes and common API paths to prevent path traversal/errors
        baseUrl = baseUrl.removeSuffix("/")
            .removeSuffix("/api/chat")
            .removeSuffix("/api/generate")
            .trim()
            .removeSuffix("/")
        
        // Ensure strictly http or https
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "http://$baseUrl"
        }
        
        // Retrofit requires base URL to end with /
        baseUrl = "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private suspend fun getApi(): OllamaApi {
        return getRetrofit().create(OllamaApi::class.java)
    }

    fun chatStream(model: String, messages: List<Message>): Flow<ChatResponse> = flow {
        val api = getApi()
        // Sanitize model name - strictly lowercase and trimmed
        val sanitizedModel = model.trim().lowercase()
        val request = ChatRequest(sanitizedModel, messages, stream = true)
        
        Log.d(TAG, "Initiating stream for model: $sanitizedModel")

        val responseBody: ResponseBody
        try {
            responseBody = api.chat(request)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            Log.e(TAG, "HTTP Error ${e.code()}: $errorBody")
            if (e.code() == 404) {
                throw IOException("Model '$sanitizedModel' not found. Verify with 'ollama list'.")
            }
            throw IOException("Server Error (${e.code()})")
        } catch (e: Exception) {
            Log.e(TAG, "Connection error", e)
            throw IOException("Network error: ${e.localizedMessage ?: "Ensure your Ollama IP is reachable."}")
        }

        try {
            val source = responseBody.source()
            val adapter = moshi.adapter(ChatResponse::class.java)
            
            while (!source.exhausted()) {
                val line = source.readUtf8Line()
                if (!line.isNullOrBlank()) {
                    try {
                        val chatResponse = adapter.fromJson(line)
                        if (chatResponse != null) {
                            emit(chatResponse)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse line: $line", e)
                        // If one line fails, we try to continue with the next
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading stream", e)
            throw IOException("Communication error: ${e.localizedMessage ?: "Unknown error"}")
        } finally {
            responseBody.close()
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun chatNonStreaming(model: String, messages: List<Message>): ChatResponse {
        val sanitizedModel = model.trim().lowercase()
        val response = getApi().chatNonStreaming(ChatRequest(sanitizedModel, messages, stream = false))
        
        if (response.isSuccessful) {
            return response.body() ?: throw IOException("Empty response")
        } else {
            if (response.code() == 404) {
                throw IOException("Model '$sanitizedModel' not found.")
            }
            throw IOException("Communication failure: ${response.code()}")
        }
    }
}
