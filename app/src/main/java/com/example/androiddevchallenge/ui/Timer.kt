package com.example.androiddevchallenge.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.R
import com.example.androiddevchallenge.ui.component.DraggableWatchFace
import com.example.androiddevchallenge.ui.component.WatchFaceState
import com.example.androiddevchallenge.ui.theme.purple200
import com.example.androiddevchallenge.ui.theme.purple500
import com.example.androiddevchallenge.ui.theme.purple700
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.launch

class TimerState {
    private val timerAngle = mutableStateOf(0f)

    val time = mutableStateOf("00:00:00")
    private var seconds = 0
    private var minutes = 0
    private var hours = 0

    val secState: WatchFaceState = WatchFaceState {
        timerAngle.value = it
        updateWatchFaces()
    }
    val minState: WatchFaceState = WatchFaceState {
        timerAngle.value = it * 60
        updateWatchFaces()
    }
    val hourState: WatchFaceState = WatchFaceState {
        timerAngle.value = it * 60 * 24
        updateWatchFaces()
    }

    private fun updateWatchFaces() {
        secState.setAngle(timerAngle.value)
        minState.setAngle(timerAngle.value / 60)
        hourState.setAngle(timerAngle.value / 60 / 24)
        seconds = (secState.angle.value / (360 / 60)).toTimeUnit(60)
        minutes = (minState.angle.value / (360 / 60)).toTimeUnit(60)
        hours = (hourState.angle.value / (360 / 24)).toTimeUnit(24)
        time.value = "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun Float.floor(): Int = kotlin.math.floor(this).toInt()

    private fun Float.toTimeUnit(size: Int): Int =
        when {
            this < 0 -> (size - this).floor().rem(size)
            else -> (size - this.rem(size)).floor().rem(size)
        }

    private fun Float.coerceInTimerRange() = when {
        this > 0 -> 24 * 60 * 360 - this
        else -> -this
    }

    fun setAngle(value: Float) {
        timerAngle.value = value
        updateWatchFaces()
    }

    fun getAngle() = -timerAngle.value.coerceInTimerRange()

    val countdownTime: Int get() = timerAngle.value.angleToSec()

    private fun Float.angleToSec(): Int = (this.coerceInTimerRange() / (360 / 60)).floor()
}

@Composable
fun Timer() {
    val step = with(LocalDensity.current) { 96.dp.toPx().toInt() }

    val timerState = remember { TimerState() }
    Box(Modifier.fillMaxSize()) {

        Text(text = timerState.time.value,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(top = 64.dp),
            style = TextStyle(fontFamily = FontFamily(Font(R.font.recursive_regular)), fontSize = 50.sp))

        DraggableWatchFace(modifier = Modifier
            .requiredSize(1100.dp)
            .watchFaceLayout(step * 2),
            state = timerState.hourState,
            color = purple700
        ) {
            getWatchFaceText(24)
        }

        DraggableWatchFace(modifier = Modifier
            .requiredSize(1100.dp)
            .watchFaceLayout(step),
            state = timerState.minState,
            color = purple500
        ) {
            getWatchFaceText()
        }

        DraggableWatchFace(modifier = Modifier
            .requiredSize(1100.dp)
            .watchFaceLayout(),
            state = timerState.secState,
            color = purple200
        ) {
            getWatchFaceText()
        }

        val countdownScope = rememberCoroutineScope()
        val animatableAngle = remember { Animatable(0f) }

        if (animatableAngle.isRunning) {
            timerState.setAngle(animatableAngle.value)
        }

        Button(modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 16.dp), onClick = {
                countdownScope.launch {
                    animatableAngle.snapTo(timerState.getAngle())
                    animatableAngle.animateTo(0f, tween(timerState.countdownTime * 1000, easing = LinearEasing))
                }
            }) {
            Text(text = "Start")
        }
    }
}

@Composable
private fun getWatchFaceText(length: Int = 60) {
    val items = (0 until length).toList()
    items.forEachIndexed { index, it ->
        BasicText("$it", modifier = Modifier.rotate(360f / items.size * index), style = TextStyle(fontFamily = FontFamily(Font(R.font.recursive_regular)), fontSize = 30.sp))
    }
}

private fun Modifier.watchFaceLayout(offset: Int = 0) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(constraints.maxWidth / 2 - placeable.width / 2, constraints.maxHeight - 1000 - offset)
    }
}