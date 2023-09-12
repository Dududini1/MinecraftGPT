package tech.lukinhas.minecraftgpt.Utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object OpenAI {

    private lateinit var apiKey: String

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    fun chatCompletion(model: String, apiKey: String?, messages: List<Pair<String, String>>, url: String): String {
        val messagesMap = messages.map { pair ->
            mapOf("content" to pair.second, "role" to pair.first)
        }
        val requestBody: Map<String, Any> = mapOf(
                "model" to model,
                "messages" to messagesMap
        )

        val json = Gson().newBuilder().setPrettyPrinting().create().toJson(requestBody)

        val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

        val request = Request.Builder()
                .url(url)
                .post(RequestBody.create("application/json".toMediaTypeOrNull(), json))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ${apiKey ?: this.apiKey}")
                .build()

        try {
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return "Request failed with code: ${response.code}"
            }

            val jsonResponse = Gson().fromJson(response.body?.string(), JsonObject::class.java)

            return jsonResponse.getAsJsonArray("choices")?.get(0)?.asJsonObject?.getAsJsonObject("message")?.get("content")?.asString
                    ?: "".also { response.body?.close() }
        } catch (e: Exception) {
            return "Request failed: ${e.message}"
        }
    }
}
