package com.example.myapplication.helpers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "budget_channel",
        "Budget Alerts",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Alerts when spending exceeds budget"
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

fun sendBudgetExceededNotification(
    context: Context,
    total: Double,
    limit: Double,
    categoryName: String
) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val builder = NotificationCompat.Builder(context, "budget_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Budget Limit Reached")
        .setContentText("You have exceeded the budget limit for $categoryName. Total: $total, Limit: $limit")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    // do sth
                }
            }
        )

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}