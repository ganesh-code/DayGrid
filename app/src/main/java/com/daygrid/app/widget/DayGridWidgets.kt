package com.daygrid.app.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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

        val isNightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
        val colorHex = fastPrefs.getString("accent_color", "#D71921") ?: "#D71921"
        val accentColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#D71921") }

        // Render high contrast, high density grid bitmap (600x400)
        val gridBitmap = createHighContrastYearGridBitmap(cells, accentColorInt, isNightMode, widthPx = 700, heightPx = 400, columns = 20)

        provideContent {
            CompactWidgetContent(
                year = year,
                today = today,
                gridBitmap = gridBitmap,
                stats = stats,
                isNightMode = isNightMode,
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

        val isNightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
        val colorHex = fastPrefs.getString("accent_color", "#D71921") ?: "#D71921"
        val accentColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#D71921") }

        // Render high resolution, high contrast grid bitmap (1000x480)
        val gridBitmap = createHighContrastYearGridBitmap(cells, accentColorInt, isNightMode, widthPx = 1000, heightPx = 480, columns = 25)

        provideContent {
            ExpandedWidgetContent(
                year = year,
                today = today,
                gridBitmap = gridBitmap,
                stats = stats,
                isNightMode = isNightMode,
                context = context
            )
        }
    }
}

class DayGridExpandedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayGridExpandedWidget()
}

// ─────────────────────────────────────────
// Nothing OS / CMF Lock Screen Squircle Widget (2x2)
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

        val isNightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val fastPrefs = context.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
        val colorHex = fastPrefs.getString("accent_color", "#D71921") ?: "#D71921"
        val accentColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#D71921") }

        // Generate authentic Nothing OS 2x2 squircle bitmap with 3 rows of dots + NDOT font text
        val squircleBitmap = createNothingSquircleBitmap(
            today = today,
            stats = stats,
            accentColor = accentColorInt,
            isNightMode = isNightMode,
            sizePx = 500
        )

        provideContent {
            NothingLockWidgetContent(
                squircleBitmap = squircleBitmap,
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
    isNightMode: Boolean,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)
    val dayName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val monthName = today.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()

    val cardBg = if (isNightMode) Color(0xFF161616) else Color(0xFFEEEEEE)
    val textPrimary = if (isNightMode) Color(0xFFF2F2F2) else Color(0xFF111111)
    val textSecondary = if (isNightMode) Color(0xFFAAAAAA) else Color(0xFF666666)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(cardBg))
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
                        color = ColorProvider(textPrimary),
                        fontSize = 12.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "${stats.productiveDays}d",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 11.sp,
                        fontWeight = androidx.glance.text.FontWeight.Medium
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
    isNightMode: Boolean,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)
    val dayName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val monthName = today.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).uppercase()
    val totalDays = if (today.isLeapYear) 366 else 365

    val cardBg = if (isNightMode) Color(0xFF141414) else Color(0xFFEFEFEF)
    val textPrimary = if (isNightMode) Color(0xFFFFFFFF) else Color(0xFF111111)
    val textSecondary = if (isNightMode) Color(0xFF888888) else Color(0xFF555555)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(cardBg))
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
                            color = ColorProvider(textPrimary),
                            fontSize = 14.sp,
                            fontWeight = androidx.glance.text.FontWeight.Bold
                        )
                    )
                    Text(
                        text = "DAY ${today.dayOfYear} OF $totalDays",
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 11.sp,
                            fontWeight = androidx.glance.text.FontWeight.Medium
                        )
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                Text(
                    text = "${stats.productiveDays} prod · ${stats.formattedPercentage}",
                    style = TextStyle(
                        color = ColorProvider(textPrimary),
                        fontSize = 12.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
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
    squircleBitmap: Bitmap,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(mainIntent)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(squircleBitmap),
            contentDescription = "Nothing DayGrid Lock Screen Widget",
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}

// ─────────────────────────────────────────
// Nothing OS Dot-Matrix Typography Engine
// ─────────────────────────────────────────

object NothingDotMatrix {
    val GLYPHS: Map<Char, List<String>> = mapOf(
        '0' to listOf(
            ".###.",
            "#...#",
            "#..##",
            "#. #.#",
            "##..#",
            "#...#",
            ".###."
        ),
        '1' to listOf(
            "..#..",
            ".##..",
            "..#..",
            "..#..",
            "..#..",
            "..#..",
            ".###."
        ),
        '2' to listOf(
            ".###.",
            "#...#",
            "....#",
            "..##.",
            ".#...",
            "#....",
            "#####"
        ),
        '3' to listOf(
            ".###.",
            "#...#",
            "....#",
            "..##.",
            "....#",
            "#...#",
            ".###."
        ),
        '4' to listOf(
            "#...#",
            "#...#",
            "#...#",
            "#####",
            "....#",
            "....#",
            "....#"
        ),
        '5' to listOf(
            "#####",
            "#....",
            "####.",
            "....#",
            "....#",
            "#...#",
            ".###."
        ),
        '6' to listOf(
            ".###.",
            "#....",
            "####.",
            "#...#",
            "#...#",
            "#...#",
            ".###."
        ),
        '7' to listOf(
            "#####",
            "....#",
            "...#.",
            "..#..",
            ".#...",
            ".#...",
            ".#..."
        ),
        '8' to listOf(
            ".###.",
            "#...#",
            "#...#",
            ".###.",
            "#...#",
            "#...#",
            ".###."
        ),
        '9' to listOf(
            ".###.",
            "#...#",
            "#...#",
            ".####",
            "....#",
            "....#",
            ".###."
        ),
        'D' to listOf(
            "####.",
            "#...#",
            "#...#",
            "#...#",
            "#...#",
            "#...#",
            "####."
        ),
        'P' to listOf(
            "####.",
            "#...#",
            "#...#",
            "####.",
            "#....",
            "#....",
            "#...."
        ),
        'H' to listOf(
            "#...#",
            "#...#",
            "#...#",
            "#####",
            "#...#",
            "#...#",
            "#...#"
        ),
        'M' to listOf(
            "#...#",
            "##.##",
            "#.#.#",
            "#...#",
            "#...#",
            "#...#",
            "#...#"
        ),
        '%' to listOf(
            "##..#",
            "##.#.",
            "..#..",
            ".#...",
            "#..##",
            "#..##",
            "....."
        ),
        ':' to listOf(
            ".....",
            "..#..",
            ".....",
            ".....",
            "..#..",
            ".....",
            "....."
        ),
        ' ' to listOf(
            ".....",
            ".....",
            ".....",
            ".....",
            ".....",
            ".....",
            "....."
        )
    )

    fun drawText(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        dotRadius: Float,
        dotSpacing: Float,
        paint: Paint
    ) {
        var cursorX = startX
        val step = dotRadius * 2f + dotSpacing

        for (ch in text.uppercase()) {
            val glyph = GLYPHS[ch] ?: GLYPHS[' ']!!
            for (row in 0 until 7) {
                val line = glyph[row]
                for (col in 0 until 5) {
                    if (col < line.length && line[col] == '#') {
                        val cx = cursorX + col * step + dotRadius
                        val cy = startY + row * step + dotRadius
                        canvas.drawCircle(cx, cy, dotRadius, paint)
                    }
                }
            }
            cursorX += 5 * step + step * 1.5f
        }
    }
}

// ─────────────────────────────────────────
// Authentic Nothing OS 2x2 Squircle Lock Screen Widget Bitmap
// (Exact match to Nothing Screen Time widget in user screenshot)
// ─────────────────────────────────────────

fun createNothingSquircleBitmap(
    today: LocalDate,
    stats: YearStats,
    accentColor: Int,
    isNightMode: Boolean,
    sizePx: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Palette based on device light/dark mode
    val bgColor = if (isNightMode) android.graphics.Color.parseColor("#181818") else android.graphics.Color.parseColor("#EDEDED")
    val borderColor = if (isNightMode) android.graphics.Color.parseColor("#2A2A2A") else android.graphics.Color.parseColor("#DFDFDF")
    val filledDotColor = if (isNightMode) android.graphics.Color.parseColor("#FFFFFF") else android.graphics.Color.parseColor("#111111")
    val unfilledDotColor = if (isNightMode) android.graphics.Color.parseColor("#343434") else android.graphics.Color.parseColor("#CCCCCC")

    // Draw Squircle background
    val rect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
    val cornerRadius = sizePx * 0.22f // Nothing OS rounded corner ratio
    paint.style = Paint.Style.FILL
    paint.color = bgColor
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f
    paint.color = borderColor
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

    // ─────────────────────────────────────────
    // Top Section: 3 rows of 14 dots (42 dots total)
    // Exactly matches Image 1 & 2 in Nothing Screen Time widget
    // ─────────────────────────────────────────
    paint.style = Paint.Style.FILL
    val topCols = 14
    val topRows = 3
    val totalDots = topCols * topRows // 42

    val totalDays = if (today.isLeapYear) 366 else 365
    val elapsedFraction = today.dayOfYear.toFloat() / totalDays.toFloat()
    val filledCount = (elapsedFraction * totalDots).toInt().coerceIn(1, totalDots)

    val topMargin = sizePx * 0.12f
    val sideMargin = sizePx * 0.12f
    val availableWidth = sizePx - 2 * sideMargin
    val dotStep = availableWidth / (topCols - 1)
    val dotRadius = dotStep * 0.38f

    for (index in 0 until totalDots) {
        val col = index % topCols
        val row = index / topCols
        val cx = sideMargin + col * dotStep
        val cy = topMargin + row * dotStep

        if (index < filledCount) {
            paint.color = filledDotColor
        } else {
            paint.color = unfilledDotColor
        }
        canvas.drawCircle(cx, cy, dotRadius, paint)
    }

    // ─────────────────────────────────────────
    // Bottom Section: Nothing Dot-Matrix Font (NDOT)
    // Matches "2H 32M" style in Image 2
    // ─────────────────────────────────────────
    val displayText = "${today.dayOfYear}D"
    val textStartX = sideMargin
    val textStartY = sizePx * 0.70f
    val textDotRadius = sizePx * 0.013f
    val textDotSpacing = sizePx * 0.007f

    paint.color = filledDotColor
    NothingDotMatrix.drawText(
        canvas = canvas,
        text = displayText,
        startX = textStartX,
        startY = textStartY,
        dotRadius = textDotRadius,
        dotSpacing = textDotSpacing,
        paint = paint
    )

    // Secondary dot matrix text on right side: e.g. "PROD" or percentage
    val subText = "${stats.productiveDays}P"
    val subStartX = sizePx * 0.62f
    paint.color = if (isNightMode) android.graphics.Color.parseColor("#888888") else android.graphics.Color.parseColor("#666666")
    NothingDotMatrix.drawText(
        canvas = canvas,
        text = subText,
        startX = subStartX,
        startY = textStartY + sizePx * 0.02f,
        dotRadius = textDotRadius * 0.85f,
        dotSpacing = textDotSpacing * 0.85f,
        paint = paint
    )

    return bitmap
}

// ─────────────────────────────────────────
// High Contrast Year Grid Canvas Generator
// (For Home Screen Expanded & Compact Widgets)
// ─────────────────────────────────────────

fun createHighContrastYearGridBitmap(
    cells: List<DayCell>,
    accentColor: Int,
    isNightMode: Boolean,
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
    val gap = cellSize * 0.20f
    val dotRadius = (cellSize - gap) / 2f

    val totalW = columns * cellSize
    val totalH = rows * cellSize
    val startX = (widthPx - totalW) / 2f
    val startY = (heightPx - totalH) / 2f

    // Contrast colors based on mode
    val pastUnmarkedColor = if (isNightMode) android.graphics.Color.parseColor("#484848") else android.graphics.Color.parseColor("#AFAFAF")
    val futureColor = if (isNightMode) android.graphics.Color.parseColor("#242424") else android.graphics.Color.parseColor("#D4D4D4")
    val unproductiveColor = if (isNightMode) android.graphics.Color.parseColor("#2E2E2E") else android.graphics.Color.parseColor("#BEBEBE")
    val neutralColor = if (isNightMode) android.graphics.Color.parseColor("#3C3C3C") else android.graphics.Color.parseColor("#CBCBCB")

    cells.forEachIndexed { index, cell ->
        val col = index % columns
        val row = index / columns
        val cx = startX + col * cellSize + cellSize / 2f
        val cy = startY + row * cellSize + cellSize / 2f

        paint.style = Paint.Style.FILL
        paint.color = when (cell.displayStatus) {
            DayCellDisplayStatus.PRODUCTIVE, DayCellDisplayStatus.TODAY_PRODUCTIVE -> accentColor
            DayCellDisplayStatus.UNPRODUCTIVE, DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> unproductiveColor
            DayCellDisplayStatus.NEUTRAL, DayCellDisplayStatus.TODAY_NEUTRAL -> neutralColor
            DayCellDisplayStatus.TODAY_UNMARKED -> pastUnmarkedColor
            DayCellDisplayStatus.PAST_UNMARKED -> pastUnmarkedColor
            DayCellDisplayStatus.FUTURE -> futureColor
        }
        canvas.drawCircle(cx, cy, dotRadius, paint)

        if (cell.isToday) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = if (isNightMode) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            canvas.drawCircle(cx, cy, dotRadius + gap * 0.45f, paint)
        }
    }

    return bitmap
}
