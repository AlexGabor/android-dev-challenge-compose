package com.example.androiddevchallenge.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WatchFace(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    content: @Composable () -> Unit,
) {
    Layout(modifier = modifier
        .clip(CircleShape)
        .background(color)
        .padding(16.dp),
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable -> measurable.measure(constraints.copy(minHeight = 0, minWidth = 0)) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val step = Math.PI * 2 / placeables.size
            val rx = constraints.maxWidth / 2
            val ry = constraints.maxHeight / 2
            placeables.forEachIndexed { index, placeable ->
                placeable.place(
                    x = (rx - placeable.width / 2) + ((rx - placeable.width / 2) * sin(Math.PI - index * step)).toInt(),
                    y = (ry - placeable.height / 2) + ((ry - placeable.height / 2) * cos(Math.PI - index * step)).toInt()
                )
            }
        }
    }
}

@Composable
fun DraggableWatchFace(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    content: @Composable () -> Unit,
) {
    var outputAngle by remember { mutableStateOf(0f) }
    var startAngle by remember { mutableStateOf(0f) }
    var prevAngle by remember { mutableStateOf(0f) }
    var coords by remember { mutableStateOf(Offset.Zero) }
    var center by remember { mutableStateOf(Offset.Zero) }

    WatchFace(modifier = modifier
        .onSizeChanged { center = it.center.toOffset() }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    prevAngle = outputAngle
                    coords = it - center
                    startAngle = coords.angle
                },
                onDragEnd = {
                },
                onDrag = { change, dragAmount ->
                    outputAngle = prevAngle + coords.angle - startAngle
                    coords += dragAmount
                    change.consumeAllChanges()
                }
            )
        }
        .rotate(outputAngle),
        color = color,
        content = content)
}

val Offset.angle get() = (atan2(y, x) * 180 / Math.PI).toFloat().rem(360)
