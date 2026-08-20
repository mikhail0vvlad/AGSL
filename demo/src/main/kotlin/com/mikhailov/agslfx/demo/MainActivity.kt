package com.mikhailov.agslfx.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import com.mikhailov.agslfx.demo.catalog.DemoEntry
import com.mikhailov.agslfx.demo.ui.AgslFxTheme
import com.mikhailov.agslfx.demo.ui.DetailScreen
import com.mikhailov.agslfx.demo.ui.GalleryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AgslFxTheme {
                AgslFxApp()
            }
        }
    }
}

@Composable
private fun AgslFxApp() {
    var selected: DemoEntry? by remember { mutableStateOf(null) }

    BackHandler(enabled = selected != null) { selected = null }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tween()) togetherWith fadeOut(tween()) },
            label = "screen",
        ) { entry ->
            if (entry == null) {
                GalleryScreen(onOpen = { selected = it })
            } else {
                DetailScreen(entry = entry, onBack = { selected = null })
            }
        }
    }
}
