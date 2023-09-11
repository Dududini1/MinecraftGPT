package tech.lukinhas.minecraftgpt.Utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
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

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
        connection.readTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer ${apiKey ?: this.apiKey}")
        connection.doOutput = true

        try {
            val os: OutputStream = connection.outputStream
            val input = json.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        } catch (e: Exception) {
            throw e
        }

        val responseCode = connection.responseCode

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("Request failed with code: $responseCode")
        }

        val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?
        while (inputStream.readLine().also { line = it } != null) {
            response.append(line)
        }
        inputStream.close()
        connection.disconnect()

        // Parse the JSON response
        val jsonResponse = Gson().fromJson(response.toString(), JsonObject::class.java)

        // Extract the content from the first choice
        return jsonResponse.getAsJsonArray("choices")?.get(0)?.asJsonObject?.getAsJsonObject("message")?.get("content")?.asString
                ?: ""
    }
}
