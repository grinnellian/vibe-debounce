package com.vibedebounce.model

import android.os.Handler
import android.os.Looper

class DebounceTimer(
    private val onExpired: (SenderKey) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var senderKey: SenderKey? = null

    fun start(key: SenderKey, delayMs: Long) {
        cancel()
        senderKey = key
        runnable = Runnable { onExpired(key) }.also {
            handler.postDelayed(it, delayMs)
        }
    }

    fun reset(delayMs: Long) {
        val key = senderKey ?: return
        cancel()
        senderKey = key
        runnable = Runnable { onExpired(key) }.also {
            handler.postDelayed(it, delayMs)
        }
    }

    fun cancel() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = null
    }
}
