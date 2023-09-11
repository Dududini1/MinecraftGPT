package tech.lukinhas.minecraftgpt.Utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object OpenAI {

    private lateinit var apiKey: String

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    fun chatCompletion(model: String, apiKey: String?, messages: List<Pair<String, String>>, url: String): String {

        val client = OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

        val messagesMap = messages.map { pair ->
            mapOf("content" to pair.second, "role" to pair.first)
        }
        val requestBody: Map<String, Any> = mapOf(
                "model" to model,
                "messages" to messagesMap
        )

        val json = Gson().newBuilder().setPrettyPrinting().create().toJson(requestBody)

        val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ${apiKey ?: this.apiKey}")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Request failed with code: ${response.code}")
        }

        val responseBody = response.body?.string() ?: ""

        // Parse the JSON response
        val jsonResponse = Gson().fromJson(responseBody, JsonObject::class.java)

        // Extract the content from the first choice

        return jsonResponse.getAsJsonArray("choices")?.get(0)?.asJsonObject?.getAsJsonObject("message")?.get("content")?.asString
                ?: "".also { response.body?.close() }
    }
}