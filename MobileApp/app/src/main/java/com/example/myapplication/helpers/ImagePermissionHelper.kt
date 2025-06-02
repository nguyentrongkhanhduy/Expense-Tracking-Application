package com.example.myapplication.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberCameraPermissionHandler(
    onSuccess: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberCameraPermissionLauncher(context, onSuccess = onSuccess)
    return {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(android.Manifest.permission.CAMERA)
        } else {
            onSuccess()
        }
    }
}

@Composable
fun rememberCameraPermissionLauncher(
    context: Context,
    onSuccess: () -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onSuccess()
            } else {
                Toast.makeText(
                    context,
                    "Camera permission denied. Please enable it in settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )
}