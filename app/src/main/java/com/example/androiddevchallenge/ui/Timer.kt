package com.example.androiddevchallenge.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.component.DraggableWatchFace

@Composable
fun Timer() {
    DraggableWatchFace(Modifier
        .requiredSize(600.dp)) {
        val items = (1..12).toList()
        items.forEachIndexed { index, it ->
            BasicText("$it", modifier = Modifier.rotate(360f / items.size * index))
        }
    }
}