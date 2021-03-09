package com.example.androiddevchallenge.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.androiddevchallenge.ui.theme.first
import com.example.androiddevchallenge.ui.theme.second
import com.example.androiddevchallenge.ui.theme.third
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

private const val MAX_TIME_ANGLE = 24 * 60 * 360

class TimerState(private val animationScope: CoroutineScope) {
    private val timerAngle = mutableStateOf(0f)

    private val animatable = Animatable(0f)
    val isRunning: Boolean get() = animatable.isRunning

    val time = mutableStateOf("00:00:00")
    private var seconds = 0
    private var minutes = 0
    private var hours = 0

    val secState: WatchFaceState = WatchFaceState {
        animationScope.coroutineContext.cancelChildren()
        timerAngle.value = it
        updateWatchFaces()
    }
    val minState: WatchFaceState = WatchFaceState {
        animationScope.coroutineContext.cancelChildren()
        timerAngle.value = it * 60
        updateWatchFaces()
    }
    val hourState: WatchFaceState = WatchFaceState {
        animationScope.coroutineContext.cancelChildren()
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
        this > 0 -> (MAX_TIME_ANGLE - this.rem(MAX_TIME_ANGLE)).rem(MAX_TIME_ANGLE)
        else -> -(this.rem(MAX_TIME_ANGLE))
    }

    private fun setAngle(value: Float) {
        timerAngle.value = value
        updateWatchFaces()
    }

    private fun getAngle() = -timerAngle.value.coerceInTimerRange()

    private val countdownTime: Int get() = timerAngle.value.angleToSec()

    private fun Float.angleToSec(): Int = (this.coerceInTimerRange() / (360 / 60)).floor()

    fun start() {
        animationScope.launch {
            animatable.snapTo(getAngle())
            animatable.animateTo(0f, tween(countdownTime * 1000, easing = LinearEasing)) {
                setAngle(this.value)
            }
        }
    }

    fun pause() {
        animationScope.coroutineContext.cancelChildren()
    }
}

@Composable
fun Timer() {
    val step = with(LocalDensity.current) { 96.dp.toPx().toInt() }

    val countdownScope = rememberCoroutineScope()
    val timerState = remember { TimerState(countdownScope) }

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
            color = third
        ) {
            getWatchFaceText(24)
        }

        DraggableWatchFace(modifier = Modifier
            .requiredSize(1100.dp)
            .watchFaceLayout(step),
            state = timerState.minState,
            color = second
        ) {
            getWatchFaceText()
        }

        DraggableWatchFace(modifier = Modifier
            .requiredSize(1100.dp)
            .watchFaceLayout(),
            state = timerState.secState,
            color = first
        ) {
            getWatchFaceText()
        }

        Button(modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
            shape = CircleShape,
            onClick = {
                countdownScope.launch {
                    if (!timerState.isRunning) {
                        timerState.start()
                    } else {
                        timerState.pause()
                    }
                }
            }) {
            val text = if (!timerState.isRunning) "Start" else "Pause"
            Text(text = text, style = MaterialTheme.typography.body2, modifier = Modifier.padding(8.dp).animateContentSize())
        }
    }
}

@Composable
private fun getWatchFaceText(length: Int = 60) {
    val items = (0 until length).toList()
    items.forEachIndexed { index, it ->
        BasicText("$it", modifier = Modifier.rotate(360f / items.size * index), style = MaterialTheme.typography.body1)
    }
}

private fun Modifier.watchFaceLayout(offset: Int = 0) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(constraints.maxWidth / 2 - placeable.width / 2, constraints.maxHeight - 1000 - offset)
    }
}