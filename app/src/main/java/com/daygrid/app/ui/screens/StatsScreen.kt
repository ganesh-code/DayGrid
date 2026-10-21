package com.daygrid.app.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    stats: YearStats?,
    accentColor: Color = DayGridOrange,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = { Text("Statistics", color = DayGridTextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DayGridTextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        if (stats == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Year + percentage hero
                Surface(
                    color = DayGridSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stats.formattedPercentage,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        Text(
                            "productive",
                            fontSize = 16.sp,
                            color = DayGridTextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${stats.year} · ${stats.elapsedDays} days elapsed",
                            fontSize = 12.sp,
                            color = DayGridTextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Productive",
                        value = stats.productiveDays.toString(),
                        color = accentColor
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Rest days",
                        value = stats.neutralDays.toString(),
                        color = DayGridNeutralCell
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Not productive",
                        value = stats.unproductiveDays.toString(),
                        color = DayGridTextTertiary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Unmarked",
                        value = stats.unmarkedElapsedDays.toString(),
                        color = DayGridPastUnmarked
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Total days",
                        value = stats.totalDays.toString(),
                        color = DayGridTextSecondary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Remaining",
                        value = stats.futureDays.toString(),
                        color = DayGridFuture.copy(alpha = 0.8f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Disclaimer
                Text(
                    "Note: Productive percentage is calculated from elapsed days only. " +
                    "Future days are never counted. Unmarked days are neither productive nor unproductive.",
                    fontSize = 11.sp,
                    color = DayGridTextTertiary,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DayGridSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = color
            )
            Text(label, fontSize = 12.sp, color = DayGridTextSecondary)
        }
    }
}
