package com.daygrid.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperScreen(
    previewBitmap: Bitmap?,
    isGenerating: Boolean,
    isApplying: Boolean,
    accentColor: Color = DayGridOrange,
    onGenerate: () -> Unit,
    onApplyLock: () -> Unit,
    onApplyHome: () -> Unit,
    onApplyBoth: () -> Unit,
    onSetLiveWallpaper: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = { Text("Wallpapers", color = DayGridTextPrimary, fontWeight = FontWeight.SemiBold) },
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
            // Dynamic Live Wallpaper Card
            // ─────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp)),
                color = DayGridSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                text = "Auto-updating clock, date & year grid",
                                color = DayGridTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Runs continuously as your Android live wallpaper. The clock ticks in real time, date updates at midnight, and your year grid is always live on your home & lock screen.",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onSetLiveWallpaper,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = DayGridBackground)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Set as Live Wallpaper",
                            color = DayGridBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ─────────────────────────────────────────
            // Static Wallpaper Generator
            // ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Static Image Wallpaper",
                    color = DayGridTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "With Date & Clock",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DayGridSurface),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isGenerating -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = accentColor)
                            Spacer(Modifier.height(8.dp))
                            Text("Generating wallpaper...", color = DayGridTextSecondary, fontSize = 13.sp)
                        }
                    }
                    previewBitmap != null -> {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Wallpaper preview",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = DayGridTextTertiary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                "Generate a high-res image with date & clock",
                                color = DayGridTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Generate button
            Button(
                onClick = onGenerate,
                enabled = !isGenerating && !isApplying,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DayGridSurface, contentColor = DayGridTextPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = accentColor)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (previewBitmap == null) "Generate Static Image" else "Regenerate Image",
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (previewBitmap != null) {
                Spacer(Modifier.height(16.dp))
                Text("Apply Static Image to:", fontSize = 13.sp, color = DayGridTextSecondary)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onApplyLock,
                        modifier = Modifier.weight(1f),
                        enabled = !isApplying,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DayGridTextSecondary)
                    ) {
                        Text("Lock screen", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onApplyHome,
                        modifier = Modifier.weight(1f),
                        enabled = !isApplying,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DayGridTextSecondary)
                    ) {
                        Text("Home screen", fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onApplyBoth,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isApplying,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = DayGridBackground)
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DayGridBackground, strokeWidth = 2.dp)
                    } else {
                        Text("Apply to both screens", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(90.dp)) // Clearance for bottom navbar
        }
    }
}
