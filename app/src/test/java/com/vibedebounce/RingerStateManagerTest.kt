package com.vibedebounce

import android.media.AudioManager
import com.vibedebounce.service.RingerStateManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RingerStateManagerTest {

    private lateinit var audioManager: AudioManager
    private lateinit var ringerStateManager: RingerStateManager

    @Before
    fun setUp() {
        audioManager = mock()
        ringerStateManager = RingerStateManager(audioManager)
    }

    @Test
    fun `mute saves ringer mode and sets silent`() {
        whenever(audioManager.ringerMode).thenReturn(AudioManager.RINGER_MODE_NORMAL)
        ringerStateManager.mute()
        verify(audioManager).ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    @Test
    fun `release restores saved ringer mode`() {
        whenever(audioManager.ringerMode).thenReturn(AudioManager.RINGER_MODE_VIBRATE)
        ringerStateManager.mute()
        ringerStateManager.release()
        verify(audioManager).ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    @Test
    fun `multiple mutes only restore after all released`() {
        whenever(audioManager.ringerMode).thenReturn(AudioManager.RINGER_MODE_NORMAL)
        ringerStateManager.mute()
        ringerStateManager.mute()
        assertEquals(2, ringerStateManager.activeCount())
        ringerStateManager.release()
        assertTrue(ringerStateManager.isActive())
        ringerStateManager.release()
        assertFalse(ringerStateManager.isActive())
    }

    @Test
    fun `already silent stays silent after release`() {
        whenever(audioManager.ringerMode).thenReturn(AudioManager.RINGER_MODE_SILENT)
        ringerStateManager.mute()
        ringerStateManager.release()
        // mute sets SILENT, release restores SILENT — both set the same value
        verify(audioManager, org.mockito.Mockito.times(2)).ringerMode = AudioManager.RINGER_MODE_SILENT
    }
}
