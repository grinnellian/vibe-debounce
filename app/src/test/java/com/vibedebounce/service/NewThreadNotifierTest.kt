package com.vibedebounce.service

import android.app.NotificationManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NewThreadNotifierTest {

    private lateinit var vibrator: Vibrator
    private lateinit var notificationManager: NotificationManager
    private lateinit var notifier: NewThreadNotifier

    @Before
    fun setUp() {
        vibrator = mock()
        notificationManager = mock()
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        notifier = NewThreadNotifier(context, vibrator, notificationManager)
    }

    @Test
    fun `fire vibrates using Vibrator API directly`() {
        notifier.fire("Alice")
        verify(vibrator).vibrate(any<VibrationEffect>())
    }

    @Test
    fun `fire posts a notification`() {
        notifier.fire("Bob")
        verify(notificationManager).notify(eq("Bob".hashCode()), any())
    }

    @Test
    fun `fire uses oneShot vibration with 200ms duration`() {
        notifier.fire("Diana")
        val expectedEffect = VibrationEffect.createOneShot(
            200, VibrationEffect.DEFAULT_AMPLITUDE
        )
        verify(vibrator).vibrate(eq(expectedEffect))
    }

    @Test
    fun `constructor takes no AudioManager parameter`() {
        // Structural test: NewThreadNotifier(Context, Vibrator, NotificationManager)
        // has no AudioManager dependency. If this compiles, the invariant holds.
        notifier.fire("Charlie")
    }

    @Test
    fun `vibrate fires a one-shot vibration without posting a notification`() {
        notifier.vibrate()

        verify(vibrator).vibrate(any<VibrationEffect>())
        verify(notificationManager, never()).notify(anyInt(), any())
    }

    @Test
    fun `vibrate uses same duration as fire`() {
        notifier.vibrate()

        val expectedEffect = VibrationEffect.createOneShot(
            NewThreadNotifier.VIBRATION_DURATION_MS,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
        verify(vibrator).vibrate(eq(expectedEffect))
    }

    @Test
    fun `fire still vibrates and posts notification after extracting vibrate`() {
        // Regression: ensure fire() behavior is unchanged
        notifier.fire("Alice")

        verify(vibrator).vibrate(any<VibrationEffect>())
        verify(notificationManager).notify(eq("Alice".hashCode()), any())
    }
}
