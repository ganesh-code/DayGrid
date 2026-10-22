package com.daygrid.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.DayCellDisplayStatus
import com.daygrid.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperScreen(
    cells: List<DayCell>,
    accentColor: Color = DayGridOrange,
    onSetLiveWallpaper: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = { Text("Live Wallpaper", color = DayGridTextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DayGridTextSecondary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ─────────────────────────────────────────
            // Live Wallpaper Phone Mockup Preview
            // ─────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .width(220.dp)
                    .height(380.dp)
                    .border(2.dp, Color(0xFF282828), RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Render miniature Year Grid preview exactly as on lockscreen
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(200.dp)
                    ) {
                        if (cells.isNotEmpty()) {
                            val columns = 25
                            val rows = (cells.size + columns - 1) / columns
                            val cellSize = minOf(size.width / columns, size.height / rows)
                            val gap = cellSize * 0.22f
                            val dotRadius = (cellSize - gap) / 2f

                            val totalGridWidth = columns * cellSize
                            val totalGridHeight = rows * cellSize
                            val startX = (size.width - totalGridWidth) / 2f
                            val startY = (size.height - totalGridHeight) / 2f

                            cells.forEachIndexed { index, cell ->
                                val col = index % columns
                                val row = index / columns
                                val cx = startX + col * cellSize + cellSize / 2f
                                val cy = startY + row * cellSize + cellSize / 2f

                                val color = when (cell.displayStatus) {
                                    DayCellDisplayStatus.PRODUCTIVE, DayCellDisplayStatus.TODAY_PRODUCTIVE -> accentColor
                                    DayCellDisplayStatus.UNPRODUCTIVE, DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> Color(0xFF262626)
                                    DayCellDisplayStatus.NEUTRAL, DayCellDisplayStatus.TODAY_NEUTRAL -> Color(0xFF383838)
                                    DayCellDisplayStatus.TODAY_UNMARKED -> Color(0xFF484848)
                                    DayCellDisplayStatus.PAST_UNMARKED -> Color(0xFF1E1E1E)
                                    DayCellDisplayStatus.FUTURE -> Color(0xFF121212)
                                }

                                drawCircle(
                                    color = color,
                                    radius = dotRadius,
                                    center = Offset(cx, cy)
                                )

                                if (cell.isToday) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = dotRadius + gap * 0.45f,
                                        center = Offset(cx, cy),
                                        style = Stroke(width = 1.2f)
                                    )
                                }
                            }
                        }
                    }

                    // Top indicator showing it adapts to lockscreen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFF222222),
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─────────────────────────────────────────
            // Live Wallpaper Description Card
            // ─────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF242424), RoundedCornerShape(16.dp)),
                color = DayGridSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dynamic Live Wallpaper",
                                color = DayGridTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Pure 365-day grid for Nothing OS & Android",
                                color = DayGridTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Clean, distraction-free Year Grid. Designed specifically for Nothing OS and Android lock screens — no duplicate clock or date text. Pure OLED black background saves battery.",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─────────────────────────────────────────
            // Apply Live Wallpaper Action Button
            // ─────────────────────────────────────────
            Button(
                onClick = onSetLiveWallpaper,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = DayGridBackground)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Set Dynamic Live Wallpaper",
                    color = DayGridBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(100.dp)) // Bottom navbar clearance
        }
    }
}
