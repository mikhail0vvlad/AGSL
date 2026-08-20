package com.mikhailov.agslfx.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikhailov.agslfx.decor.animatedBorder
import com.mikhailov.agslfx.demo.catalog.DemoCatalog
import com.mikhailov.agslfx.demo.catalog.DemoEntry
import com.mikhailov.agslfx.demo.catalog.DemoGroups
import com.mikhailov.agslfx.demo.catalog.defaultValues

@Composable
fun GalleryScreen(
    onOpen: (DemoEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            GalleryHeader()
        }

        DemoGroups.forEach { group ->
            val entries = DemoCatalog.filter { it.group == group }
            if (entries.isEmpty()) return@forEach

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = group,
                    modifier = Modifier.padding(top = 16.dp, bottom = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            items(entries, key = { it.id }) { entry ->
                GalleryCard(entry = entry, onClick = { onOpen(entry) })
            }
        }
    }
}

@Composable
private fun GalleryHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0C0F1C))
                .animatedBorder(cornerRadius = 24.dp, width = 1.5.dp, glow = 0.5f, speed = 0.7f)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "AGSL FX",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Библиотека шейдерных эффектов для Jetpack Compose.\n" +
                        "${DemoCatalog.size} эффектов, всё на AGSL, без единого drawable.",
                    color = Color(0xFF9AA3BD),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun GalleryCard(entry: DemoEntry, onClick: () -> Unit) {
    val values = remember(entry) { entry.defaultValues() }
    Column(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
        ) {
            entry.preview(values, Modifier.fillMaxSize())
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = entry.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = entry.hint ?: "${entry.params.size} параметра".takeIf { entry.params.isNotEmpty() }
                    ?: "без параметров",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}
