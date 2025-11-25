package com.umbra.umbradex.util

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var backgroundPlayer: MediaPlayer? = null

    suspend fun playSound(fileName: String, loop: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                // Stop previous sound if playing
                mediaPlayer?.release()

                val resId = context.resources.getIdentifier(
                    fileName.replace(".wav", ""),
                    "raw",
                    context.packageName
                )

                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(context, resId).apply {
                        isLooping = loop
                        start()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun playBackgroundMusic() {
        withContext(Dispatchers.IO) {
            try {
                if (backgroundPlayer?.isPlaying == true) return@withContext

                val resId = context.resources.getIdentifier(
                    "background",
                    "raw",
                    context.packageName
                )

                if (resId != 0) {
                    backgroundPlayer = MediaPlayer.create(context, resId).apply {
                        isLooping = true
                        setVolume(0.3f, 0.3f) // Lower volume for background
                        start()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopBackgroundMusic() {
        backgroundPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        backgroundPlayer = null
    }

    fun stopSound() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    fun pauseBackgroundMusic() {
        backgroundPlayer?.pause()
    }

    fun resumeBackgroundMusic() {
        backgroundPlayer?.start()
    }

    fun release() {
        stopSound()
        stopBackgroundMusic()
    }
}