package com.daygrid.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.daygrid.app.MainActivity
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.DayCellDisplayStatus
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.domain.usecase.GetYearGridUseCase
import com.daygrid.app.domain.usecase.GetYearStatsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────
// Entry point for Glance widget
// ─────────────────────────────────────────
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getYearGridUseCase(): GetYearGridUseCase
    fun getYearStatsUseCase(): GetYearStatsUseCase
}

// ─────────────────────────────────────────
// Compact Widget (Home Screen & Lock Screen)
// ─────────────────────────────────────────
class DayGridCompactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val today = LocalDate.now()
        val year = today.year
        val cells = entryPoint.getYearGridUseCase()(year).first()
        val stats = entryPoint.getYearStatsUseCase()(year).first()

        val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
        val colorHex = fastPrefs.getString("accent_color", "#FF8A00") ?: "#FF8A00"
        val accentColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#FF8A00") }

        val gridBitmap = createGridBitmap(cells, accentColorInt, 400, 240, columns = 20)

        provideContent {
            CompactWidgetContent(
                year = year,
                today = today,
                gridBitmap = gridBitmap,
                stats = stats,
                context = context
            )
        }
    }
}

class DayGridCompactWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayGridCompactWidget()
}

// ─────────────────────────────────────────
// Expanded Widget (Home Screen)
// ─────────────────────────────────────────
class DayGridExpandedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val today = LocalDate.now()
        val year = today.year
        val cells = entryPoint.getYearGridUseCase()(year).first()
        val stats = entryPoint.getYearStatsUseCase()(year).first()

        val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
        val colorHex = fastPrefs.getString("accent_color", "#FF8A00") ?: "#FF8A00"
        val accentColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#FF8A00") }

        val gridBitmap = createGridBitmap(cells, accentColorInt, 700, 320, columns = 25)

        provideContent {
            ExpandedWidgetContent(
                year = year,
                today = today,
                gridBitmap = gridBitmap,
                stats = stats,
                context = context
            )
        }
    }
}

class DayGridExpandedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayGridExpandedWidget()
}

// ─────────────────────────────────────────
// Nothing OS / CMF Lock Screen Screen Time Widget
// ─────────────────────────────────────────
class NothingScreenTimeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val today = LocalDate.now()
        val year = today.year
        val stats = entryPoint.getYearStatsUseCase()(year).first()

        val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
        val colorHex = fastPrefs.getString("accent_color", "#D71921") ?: "#D71921"
        val accentColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#D71921") }

        // Generate Nothing Screen Time style circle bitmap
        val circleBitmap = createNothingScreenTimeBitmap(today, stats, accentColorInt, sizePx = 200)

        provideContent {
            NothingLockWidgetContent(
                today = today,
                stats = stats,
                circleBitmap = circleBitmap,
                context = context
            )
        }
    }
}

class NothingScreenTimeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NothingScreenTimeWidget()
}

// ─────────────────────────────────────────
// UI Composables for Widgets
// ─────────────────────────────────────────

@Composable
private fun CompactWidgetContent(
    year: Int,
    today: LocalDate,
    gridBitmap: Bitmap,
    stats: YearStats,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)
    val dayName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val monthName = today.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF101010)))
            .clickable(actionStartActivity(mainIntent))
            .padding(10.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "$year · $dayName ${today.dayOfMonth} $monthName",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF0F0F0)),
                        fontSize = 12.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "${stats.productiveDays}d",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFAAAAAA)),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "DayGrid Year Grid",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
            )
        }
    }
}

@Composable
private fun ExpandedWidgetContent(
    year: Int,
    today: LocalDate,
    gridBitmap: Bitmap,
    stats: YearStats,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)
    val dayName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val monthName = today.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val totalDays = if (today.isLeapYear) 366 else 365

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF0E0E0E)))
            .clickable(actionStartActivity(mainIntent))
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$dayName, ${today.dayOfMonth} $monthName $year",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFFFFF)),
                            fontSize = 14.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold
                        )
                    )
                    Text(
                        text = "DAY ${today.dayOfYear} OF $totalDays",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF888888)),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                Text(
                    text = "${stats.productiveDays} prod · ${stats.formattedPercentage}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFCCCCCC)),
                        fontSize = 12.sp,
                        fontWeight = androidx.glance.text.FontWeight.Medium
                    )
                )
            }

            Spacer(GlanceModifier.height(8.dp))

            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "DayGrid Year Grid",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
            )
        }
    }
}

@Composable
private fun NothingLockWidgetContent(
    today: LocalDate,
    stats: YearStats,
    circleBitmap: Bitmap,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)
    val totalDays = if (today.isLeapYear) 366 else 365

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xCC000000)))
            .clickable(actionStartActivity(mainIntent))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(circleBitmap),
                contentDescription = "Nothing Screen Time Grid Widget",
                modifier = GlanceModifier.size(48.dp)
            )

            Spacer(GlanceModifier.width(6.dp))

            Column(
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "DAY ${today.dayOfYear}/$totalDays",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF2F2F2)),
                        fontSize = 11.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
                Text(
                    text = "${stats.productiveDays} PROD · ${stats.formattedPercentage}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFA0A0A0)),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────
// High-performance Bitmap Canvas Generators
// ─────────────────────────────────────────

fun createGridBitmap(
    cells: List<DayCell>,
    accentColor: Int,
    widthPx: Int,
    heightPx: Int,
    columns: Int = 25
): Bitmap {
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (cells.isEmpty()) return bitmap

    val rows = (cells.size + columns - 1) / columns
    val cellSize = minOf(widthPx.toFloat() / columns, heightPx.toFloat() / rows)
    val gap = cellSize * 0.22f
    val dotRadius = (cellSize - gap) / 2f

    val totalW = columns * cellSize
    val totalH = rows * cellSize
    val startX = (widthPx - totalW) / 2f
    val startY = (heightPx - totalH) / 2f

    cells.forEachIndexed { index, cell ->
        val col = index % columns
        val row = index / columns
        val cx = startX + col * cellSize + cellSize / 2f
        val cy = startY + row * cellSize + cellSize / 2f

        paint.style = Paint.Style.FILL
        paint.color = when (cell.displayStatus) {
            DayCellDisplayStatus.PRODUCTIVE, DayCellDisplayStatus.TODAY_PRODUCTIVE -> accentColor
            DayCellDisplayStatus.UNPRODUCTIVE, DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> android.graphics.Color.parseColor("#262626")
            DayCellDisplayStatus.NEUTRAL, DayCellDisplayStatus.TODAY_NEUTRAL -> android.graphics.Color.parseColor("#383838")
            DayCellDisplayStatus.TODAY_UNMARKED -> android.graphics.Color.parseColor("#555555")
            DayCellDisplayStatus.PAST_UNMARKED -> android.graphics.Color.parseColor("#1C1C1C")
            DayCellDisplayStatus.FUTURE -> android.graphics.Color.parseColor("#121212")
        }
        canvas.drawCircle(cx, cy, dotRadius, paint)

        if (cell.isToday) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.5f
            paint.color = android.graphics.Color.WHITE
            canvas.drawCircle(cx, cy, dotRadius + 1.5f, paint)
        }
    }

    return bitmap
}

fun createNothingScreenTimeBitmap(
    today: LocalDate,
    stats: YearStats,
    accentColor: Int,
    sizePx: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val cx = sizePx / 2f
    val cy = sizePx / 2f
    val strokeW = sizePx * 0.10f
    val radius = (sizePx - strokeW) / 2f

    // Background track ring
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = strokeW
    paint.color = android.graphics.Color.parseColor("#252525")
    canvas.drawCircle(cx, cy, radius, paint)

    // Progress arc
    val totalDays = if (today.isLeapYear) 366 else 365
    val sweepAngle = 360f * (today.dayOfYear.toFloat() / totalDays.toFloat())
    paint.color = accentColor
    paint.strokeCap = Paint.Cap.ROUND
    val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
    canvas.drawArc(rect, -90f, sweepAngle, false, paint)

    // Center number (day of year or productive days)
    paint.style = Paint.Style.FILL
    paint.textAlign = Paint.Align.CENTER
    paint.color = android.graphics.Color.parseColor("#FFFFFF")
    paint.textSize = sizePx * 0.32f
    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    val numText = "${today.dayOfYear}"
    canvas.drawText(numText, cx, cy + sizePx * 0.08f, paint)

    // Subtext "DAY"
    paint.textSize = sizePx * 0.13f
    paint.color = android.graphics.Color.parseColor("#888888")
    canvas.drawText("DAY", cx, cy + sizePx * 0.24f, paint)

    // Signature Nothing Red dot at 12 o'clock
    paint.color = android.graphics.Color.parseColor("#D71921")
    canvas.drawCircle(cx, strokeW / 2f, strokeW * 0.35f, paint)

    return bitmap
}
