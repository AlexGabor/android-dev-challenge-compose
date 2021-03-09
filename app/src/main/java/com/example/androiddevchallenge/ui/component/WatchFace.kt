/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
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
    Layout(
        modifier = modifier
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
fun rememberWatchFaceState(onDrag: (Float) -> Unit): WatchFaceState {
    val onDragState = rememberUpdatedState(onDrag)
    return remember { WatchFaceState { onDragState.value.invoke(it) } }
}

class WatchFaceState(private val onDrag: (Float) -> Unit = {}) {
    private val _angle: MutableState<Float> = mutableStateOf(0f)
    val angle: State<Float> = _angle
    private var startAngle = (0f)
    private var prevAngle = (0f)
    private var coords = (Offset.Zero)
    private var center = (Offset.Zero)
    private var isDragging = false

    fun setAngle(angle: Float) {
        _angle.value = angle
    }

    fun setFaceCenter(offset: Offset) {
        center = offset
    }

    fun onDragStart(offset: Offset) {
        prevAngle = angle.value
        coords = offset - center
        startAngle = coords.angle
        isDragging = true
    }

    fun onDragEnd() {
        isDragging = false
    }

    fun onUserDrag(change: PointerInputChange, dragAmount: Offset) {
        if (isDragging) {
            _angle.value = prevAngle + coords.angle - startAngle
            onDrag(angle.value)
            coords += dragAmount
        }
        change.consumeAllChanges()
    }
}

@Composable
fun DraggableWatchFace(
    modifier: Modifier = Modifier,
    state: WatchFaceState,
    color: Color = MaterialTheme.colors.primary,
    content: @Composable () -> Unit,
) {

    WatchFace(
        modifier = modifier
            .onSizeChanged { state.setFaceCenter(it.center.toOffset()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { state.onDragStart(it) },
                    onDragEnd = { state.onDragEnd() },
                    onDrag = { change, dragAmount -> state.onUserDrag(change, dragAmount) }
                )
            }
            .rotate(state.angle.value),
        color = color,
        content = content
    )
}

val Offset.angle get() = (atan2(y, x) * 180 / Math.PI).toFloat().rem(360)
