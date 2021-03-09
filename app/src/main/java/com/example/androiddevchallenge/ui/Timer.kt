package com.example.androiddevchallenge.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

class TimerState {
    private val secAngle = mutableStateOf(0f)

    val time = mutableStateOf("00:00:00")
    private var seconds = 0
    private var minutes = 0
    private var hours = 0

    val secState: WatchFaceState = WatchFaceState {
        secAngle.value = it
        updateWatchFaces()
    }
    val minState: WatchFaceState = WatchFaceState {
        secAngle.value = it * 60
        updateWatchFaces()
    }
    val hourState: WatchFaceState = WatchFaceState {
        secAngle.value = it * 60 * 24
        updateWatchFaces()
    }

    private fun updateWatchFaces() {
        secState.setAngle(secAngle.value)
        minState.setAngle(secAngle.value / 60)
        hourState.setAngle(secAngle.value / 60 / 24)
        seconds = (secState.angle.value / (360 / 60)).toTimeUnit(60)
        minutes = (minState.angle.value / (360 / 60)).toTimeUnit(60)
        hours = (hourState.angle.value / (360 / 24)).toTimeUnit(24)
        time.value = "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun Float.floor(): Int = kotlin.math.floor(this).toInt()

    private fun Float.toTimeUnit(other: Int): Int =
        when {
            this < 0 -> (other - this).floor().rem(other)
            else -> (other - this.rem(other)).floor()
        }
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

        Button(modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 16.dp), onClick = { /*TODO*/ }) {
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