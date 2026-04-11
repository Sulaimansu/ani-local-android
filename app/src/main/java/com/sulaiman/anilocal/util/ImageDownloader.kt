package com.sulaiman.anilocal.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageDownloader {
    private const val POSTER_DIR = "posters"
    private const val BANNER_DIR = "banners"

    fun getPosterDir(context: Context): File {
        val dir = File(context.filesDir, POSTER_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getBannerDir(context: Context): File {
        val dir = File(context.filesDir, BANNER_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPosterPath(context: Context, animeId: Int): File {
        return File(getPosterDir(context), "anime_${animeId}.jpg")
    }

    fun getBannerPath(context: Context, animeId: Int): File {
        return File(getBannerDir(context), "anime_${animeId}.jpg")
    }

    suspend fun downloadAndSave(url: String?, destFile: File): Boolean {
        if (url.isNullOrEmpty()) return false
        return try {
            withContext(Dispatchers.IO) {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                val bitmap = BitmapFactory.decodeStream(connection.getInputStream())
                if (bitmap != null) {
                    FileOutputStream(destFile).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                    }
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getLocalUri(file: File): String? {
        return if (file.exists()) "file://${file.absolutePath}" else null
    }
}
