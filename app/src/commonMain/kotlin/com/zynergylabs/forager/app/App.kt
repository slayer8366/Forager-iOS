package com.zynergylabs.forager.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color

/** A1's one colour: saturated and not a system colour, so neither a home screen nor a blank window can match it. */
val A1_COLOUR = Color(0xFF1E6FD9)

/** The line A1's checks wait for; printed once, from the draw phase. */
const val FIRST_FRAME_LINE = "A1: first frame drawn"

/**
 * The empty app: one colour over the whole screen. The colour and the line come from the same draw lambda, so the line
 * is printed only by a frame that drew the colour, never by composition alone.
 */
@Composable
fun App() {
    Box(
        Modifier.fillMaxSize().drawBehind {
            drawRect(A1_COLOUR)
            FirstFrame.logOnce()
        },
    )
}

/** Draw runs on the main thread; one flag is enough to print once per process. */
private object FirstFrame {
    private var logged = false

    fun logOnce() {
        if (logged) return
        logged = true
        println(FIRST_FRAME_LINE)
    }
}
