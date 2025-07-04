package com.example.myapplication.helpers

import okhttp3.*
import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun askGemini(question: String, onResult: (String) -> Unit) {
    val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-pro",

        /*---- For Physical Device  ----*/
        apiKey = BuildConfig.GEMINI_API_KEY

        /*---- For Android Studio  ----*/
        //apiKey = "" //API Key here
    )
    MainScope().launch {
        try {
            val response = generativeModel.generateContent(question)
            onResult(response.text.toString())
        } catch (e: Exception) {
            onResult("Failed: ${e.message}")
        }
    }
}

