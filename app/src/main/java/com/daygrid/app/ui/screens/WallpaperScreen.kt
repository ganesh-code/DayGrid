package com.daygrid.app.ui.screens

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = { Text("Wallpaper", color = DayGridTextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DayGridTextSecondary)
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
            Spacer(Modifier.height(12.dp))

            // Preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(20.dp))
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
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = DayGridTextTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Generate a wallpaper from your year grid",
                                color = DayGridTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Generate button
            Button(
                onClick = onGenerate,
                enabled = !isGenerating && !isApplying,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (previewBitmap == null) "Generate Wallpaper" else "Regenerate",
                    color = DayGridBackground,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (previewBitmap != null) {
                Spacer(Modifier.height(16.dp))
                Text("Apply to:", fontSize = 13.sp, color = DayGridTextSecondary)
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
                    colors = ButtonDefaults.buttonColors(containerColor = DayGridSurface, contentColor = DayGridTextPrimary)
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DayGridTextPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Apply to both", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Surface(
                    color = DayGridSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "⚠ The wallpaper is a static image. After recording a new day, regenerate and reapply it to reflect changes. " +
                        "Lock-screen wallpaper availability depends on your device's Android version.",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 11.sp,
                        color = DayGridTextTertiary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
