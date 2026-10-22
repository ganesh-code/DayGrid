package com.daygrid.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.DayCellDisplayStatus
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.ui.theme.*
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// YEAR GRID SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearGridScreen(
    year: Int,
    cells: List<DayCell>,
    stats: YearStats?,
    isLoading: Boolean,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onCheckIn: () -> Unit,
    onCellTap: (DayCell) -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateWallpaper: () -> Unit,
    onNavigateSettings: () -> Unit,
    accentColor: Color = DayGridOrange
) {
    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            DayGridTopBar(
                year = year,
                onPreviousYear = onPreviousYear,
                onNextYear = onNextYear,
                onNavigateStats = onNavigateStats,
                onNavigateWallpaper = onNavigateWallpaper,
                onNavigateSettings = onNavigateSettings
            )
        },
        floatingActionButton = {
            CheckInFab(onClick = onCheckIn, accentColor = accentColor)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            // Stats summary row
            if (stats != null) {
                StatsRow(stats = stats, accentColor = accentColor)
                Spacer(Modifier.height(16.dp))
            }

            // Grid
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DayGridOrange)
                }
            } else {
                YearGrid(
                    cells = cells,
                    accentColor = accentColor,
                    onCellTap = onCellTap
                )
            }

            // Legend
            Spacer(Modifier.height(16.dp))
            GridLegend()
            Spacer(Modifier.height(100.dp)) // FAB clearance
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayGridTopBar(
    year: Int,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateWallpaper: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DayGridBackground,
            titleContentColor = DayGridTextPrimary
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DayGrid",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DayGridTextPrimary
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DayGridSurface)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(onClick = onPreviousYear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Year", tint = DayGridTextPrimary, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = year.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DayGridTextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onNextYear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Year", tint = DayGridTextPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
        }
    )
}

@Composable
private fun StatsRow(stats: YearStats, accentColor: Color) {
    val today = remember { java.time.LocalDate.now() }
    val isCurrentYear = stats.year == today.year
    val dayOfWeekName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()).uppercase()
    val monthName = today.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase()
    val totalDays = if (today.isLeapYear) 366 else 365

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Today's Date & Year
        Column {
            if (isCurrentYear) {
                Text(
                    text = "$dayOfWeekName, ${today.dayOfMonth} $monthName",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = accentColor
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stats.year.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = DayGridTextPrimary
                )
                if (isCurrentYear) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = DayGridSurface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "DAY ${today.dayOfYear} OF $totalDays",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DayGridTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Stats chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip(
                value = stats.productiveDays.toString(),
                label = "productive",
                color = accentColor
            )
            StatChip(
                value = stats.formattedPercentage,
                label = "of elapsed",
                color = DayGridTextSecondary
            )
        }
    }
}

@Composable
private fun StatChip(value: String, label: String, color: Color) {
    Surface(
        color = DayGridSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 11.sp, color = DayGridTextSecondary)
        }
    }
}

@Composable
fun YearGrid(
    cells: List<DayCell>,
    accentColor: Color,
    onCellTap: (DayCell) -> Unit,
    cellSize: Dp = 14.dp,
    gap: Dp = 3.dp,
    columns: Int = 25,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        cells.chunked(columns).forEach { rowCells ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                rowCells.forEach { cell ->
                    DayDot(
                        cell = cell,
                        accentColor = accentColor,
                        size = cellSize,
                        onClick = { onCellTap(cell) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayDot(
    cell: DayCell,
    accentColor: Color,
    size: Dp,
    onClick: () -> Unit
) {
    val (fillColor, hasRing, ringColor) = when (cell.displayStatus) {
        DayCellDisplayStatus.PRODUCTIVE        -> Triple(accentColor, false, Color.Transparent)
        DayCellDisplayStatus.TODAY_PRODUCTIVE  -> Triple(accentColor, true, accentColor)
        DayCellDisplayStatus.UNPRODUCTIVE      -> Triple(DayGridUnproductive, false, Color.Transparent)
        DayCellDisplayStatus.TODAY_UNPRODUCTIVE-> Triple(DayGridUnproductive, true, DayGridTextTertiary)
        DayCellDisplayStatus.NEUTRAL           -> Triple(DayGridNeutralMark, false, Color.Transparent)
        DayCellDisplayStatus.TODAY_NEUTRAL     -> Triple(DayGridNeutralMark, true, DayGridTextSecondary)
        DayCellDisplayStatus.TODAY_UNMARKED    -> Triple(DayGridBackground, true, DayGridTextSecondary)
        DayCellDisplayStatus.PAST_UNMARKED     -> Triple(DayGridPastUnmarked, false, Color.Transparent)
        DayCellDisplayStatus.FUTURE            -> Triple(DayGridFuture, false, Color.Transparent)
    }

    val formatter = DateTimeFormatter.ofPattern("d MMM")
    val a11yDesc = buildString {
        append(cell.date.format(formatter))
        append(": ")
        append(when (cell.displayStatus) {
            DayCellDisplayStatus.PRODUCTIVE, DayCellDisplayStatus.TODAY_PRODUCTIVE -> "productive"
            DayCellDisplayStatus.UNPRODUCTIVE, DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> "not productive"
            DayCellDisplayStatus.NEUTRAL, DayCellDisplayStatus.TODAY_NEUTRAL -> "rest"
            DayCellDisplayStatus.TODAY_UNMARKED -> "today, unmarked"
            DayCellDisplayStatus.PAST_UNMARKED -> "unmarked"
            DayCellDisplayStatus.FUTURE -> "future"
        })
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (hasRing) Modifier.border(1.5.dp, ringColor, CircleShape)
                else Modifier
            )
            .background(fillColor, CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = a11yDesc }
    )
}

@Composable
private fun CheckInFab(onClick: () -> Unit, accentColor: Color) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = accentColor,
        contentColor = DayGridBackground,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("How was today?", fontWeight = FontWeight.SemiBold) }
    )
}

@Composable
private fun GridLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem(color = DayGridOrange, label = "Productive")
        LegendItem(color = DayGridNeutralMark, label = "Rest")
        LegendItem(color = DayGridUnproductive, label = "Not productive")
        LegendItem(color = DayGridPastUnmarked, label = "Unmarked")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(label, fontSize = 10.sp, color = DayGridTextTertiary)
    }
}
