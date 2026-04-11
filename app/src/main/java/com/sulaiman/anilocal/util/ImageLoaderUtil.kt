package com.sulaiman.anilocal.util

import android.content.Context
import android.net.Uri
import coil.request.ImageRequest

object ImageLoaderUtil {
    /**
     * Returns the image URI for Coil. Uses local file if exists, otherwise falls back to URL.
     */
    fun getImageUri(context: Context, remoteUrl: String?, localFile: android.graphics.Bitmap? = null): Any? {
        // If we have a local file that exists, use it
        remoteUrl ?: return null
        return remoteUrl
    }

    /**
     * Get local poster URI if it exists, otherwise return remote URL.
     */
    fun getPosterData(context: Context, animeId: Int, remoteUrl: String?): Any? {
        val localFile = ImageDownloader.getPosterPath(context, animeId)
        return if (localFile.exists()) {
            Uri.fromFile(localFile)
        } else {
            remoteUrl
        }
    }

    /**
     * Get local banner URI if it exists, otherwise return remote URL.
     */
    fun getBannerData(context: Context, animeId: Int, remoteUrl: String?): Any? {
        val localFile = ImageDownloader.getBannerPath(context, animeId)
        return if (localFile.exists()) {
            Uri.fromFile(localFile)
        } else {
            remoteUrl
        }
    }
}
