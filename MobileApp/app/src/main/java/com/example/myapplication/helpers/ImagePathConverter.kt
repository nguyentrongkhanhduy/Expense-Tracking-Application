package com.example.myapplication.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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