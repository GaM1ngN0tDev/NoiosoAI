package com.noioso.noiosoai.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming
import java.util.UUID

interface OllamaApi {
    @Streaming
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): ResponseBody

    @POST("api/chat")
    suspend fun chatNonStreaming(@Body request: ChatRequest): retrofit2.Response<ChatResponse>
}

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = true
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String,
    @Transient val id: String = UUID.randomUUID().toString()
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val model: String,
    @param:Json(name = "created_at") val createdAt: String,
    val message: Message?,
    val done: Boolean,
    @param:Json(name = "total_duration") val totalDuration: Long? = null,
    @param:Json(name = "load_duration") val loadDuration: Long? = null,
    @param:Json(name = "prompt_eval_count") val promptEvalCount: Int? = null,
    @param:Json(name = "prompt_eval_duration") val promptEvalDuration: Long? = null,
    @param:Json(name = "eval_count") val evalCount: Int? = null,
    @param:Json(name = "eval_duration") val evalDuration: Long? = null
)
