package com.mikhailov.agslfx.demo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Тестовая «картинка», к которой применяются эффекты над контентом.
 * Рисуется целиком кодом, чтобы демо не тащило за собой ресурсы.
 */
@Composable
fun SampleArtwork(modifier: Modifier = Modifier) {
    Box(
        modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1B1E4B),
                    Color(0xFF6A2C86),
                    Color(0xFFE0574A),
                    Color(0xFFF7B733),
                ),
                start = Offset.Zero,
                end = Offset.Infinite,
            )
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension
            drawCircle(
                color = Color(0xFFFFE29A).copy(alpha = 0.9f),
                radius = r * 0.17f,
                center = Offset(size.width * 0.74f, size.height * 0.26f),
            )
            drawCircle(
                color = Color(0xFF0B0F2B).copy(alpha = 0.55f),
                radius = r * 0.42f,
                center = Offset(size.width * 0.18f, size.height * 0.86f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = r * 0.3f,
                center = Offset(size.width * 0.55f, size.height * 0.62f),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "AGSL",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
            )
            Text(
                text = "Android Graphics\nShading Language",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
