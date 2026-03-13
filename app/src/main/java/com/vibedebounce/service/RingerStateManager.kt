package com.vibedebounce.service

import android.media.AudioManager

class RingerStateManager(private val audioManager: AudioManager) {

    private var savedRingerMode: Int? = null
    private var activeDebounceCount = 0

    @Synchronized
    fun mute() {
        if (activeDebounceCount == 0) {
            savedRingerMode = audioManager.ringerMode
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }
        activeDebounceCount++
    }

    @Synchronized
    fun release() {
        activeDebounceCount = maxOf(0, activeDebounceCount - 1)
        if (activeDebounceCount == 0) {
            savedRingerMode?.let { audioManager.ringerMode = it }
            savedRingerMode = null
        }
    }

    @Synchronized
    fun releaseAll() {
        if (activeDebounceCount > 0) {
            activeDebounceCount = 0
            savedRingerMode?.let { audioManager.ringerMode = it }
            savedRingerMode = null
        }
    }

    @Synchronized
    fun isActive(): Boolean = activeDebounceCount > 0

    @Synchronized
    fun activeCount(): Int = activeDebounceCount
}
