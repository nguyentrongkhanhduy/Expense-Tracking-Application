package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.helpers.RequestNotificationPermission
import com.example.myapplication.helpers.createNotificationChannel
import com.example.myapplication.navigation.AppNavHost
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        MobileAds.initialize(this) {}
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        createNotificationChannel(applicationContext)
        setContent {
            RequestNotificationPermission()
            AppNavHost()
        }
    }
}

