package com.example.myapplication.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.myapplication.services.RequestedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
    return try {
        val filename = "receipt_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file.absolutePath // return file path
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun loadImageUriOrBitmapFromInternalStorage(context: Context, filename: String): Any? {
    return try {
        when {
            filename.startsWith("bitmap:") -> {
                val path = filename.removePrefix("bitmap:")
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            }

            filename.startsWith("uri:") -> {
                Uri.parse(filename.removePrefix("uri:"))
            }

            else -> null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun removeFromInternalStorage(filename: String): Boolean {
    return try {
        val file = File(filename.removePrefix("bitmap:"))
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun getRequestedImage(
    context: Context,
    uri: Uri?,
    bitmap: Bitmap?,
    imageName: String
): RequestedImage? {
    return try {
        val byteArray = when {
            bitmap != null -> {
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.toByteArray()
                }
            }
            uri != null -> {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
            }
            else -> null
        } ?: return null

        val base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)

        val contentType = when {
            bitmap != null -> "image/jpeg"
            uri != null -> context.contentResolver.getType(uri) ?: "image/jpeg"
            else -> "image/jpeg"
        }

        RequestedImage(imageName, base64, contentType)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}