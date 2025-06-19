package com.example.myapplication.helpers

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.myapplication.BuildConfig

/*---- For Android Studio  ----*/
const val HF_API_TOKEN = ""

fun askHuggingFace(question: String, onResult: (String) -> Unit) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val url = "https://api-inference.huggingface.co/models/HuggingFaceH4/zephyr-7b-beta"

    /*---- For Physical Device  ----*/
    val apiKey = BuildConfig.HF_API_TOKEN

    val json = JSONObject().apply {
        put("inputs", question)
    }
    val body = RequestBody.create(
        "application/json".toMediaTypeOrNull(), json.toString()
    )
    val request = Request.Builder()
        .url(url)
        /*---- For Android Studio  ----*/
        .header("Authorization", "Bearer $HF_API_TOKEN")

        /*---- For Physical Device  ----*/
//        .header("Authorization", "Bearer $apiKey")

        .post(body)
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult("Failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val bodyString = response.body?.string()
            if (response.code == 403) {
                onResult("Error: 403 Forbidden. Check your Hugging Face API token and model access.")
                return
            }
            if (bodyString != null) {
                try {
                    val arr = JSONArray(bodyString)
                    val aiReply = arr.getJSONObject(0).getString("generated_text")
                    onResult(aiReply.trim())
                } catch (e: Exception) {
                    onResult("Unexpected response: $bodyString")
                }
            } else {
                onResult("No response from AI.")
            }
        }
    })
}
