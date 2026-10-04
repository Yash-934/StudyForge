package com.studyforge.app.ui.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds left and right edge swipe-to-go-back gesture navigation.
 * Left edge swipe to right, or right edge swipe to left will trigger [onBack].
 * Non-edge touches are untouched so inner scrolling and typing remain unaffected.
 */
fun Modifier.swipeBackGesture(
    enabled: Boolean = true,
    edgeThreshold: Dp = 48.dp,
    swipeThreshold: Dp = 56.dp,
    onBack: () -> Unit
): Modifier = if (!enabled) this else this.pointerInput(enabled) {
    val edgeThresholdPx = edgeThreshold.toPx()
    val swipeThresholdPx = swipeThreshold.toPx()

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val startX = down.position.x
        val width = size.width
        val isLeftEdge = startX <= edgeThresholdPx
        val isRightEdge = startX >= (width - edgeThresholdPx)

        if (!isLeftEdge && !isRightEdge) {
            // Not started at edge, allow normal children interaction
            return@awaitEachGesture
        }

        var totalDragX = 0f
        val pointerId = down.id
        var triggered = false

        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.find { it.id == pointerId } ?: break
            if (!change.pressed) break

            val dragX = change.position.x - change.previousPosition.x
            totalDragX += dragX

            if (isLeftEdge && totalDragX >= swipeThresholdPx && !triggered) {
                triggered = true
                change.consume()
                onBack()
                break
            } else if (isRightEdge && totalDragX <= -swipeThresholdPx && !triggered) {
                triggered = true
                change.consume()
                onBack()
                break
            }
        }
    }
}
