package com.daygrid.app.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.os.Build
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.DayCellDisplayStatus
import com.daygrid.app.domain.model.YearStats
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Generates a high-quality portrait wallpaper bitmap from the year grid.
     *
     * Safe zones: top 15% and bottom 20% are kept clear for clock and notifications.
     * Grid is rendered in the middle safe zone.
     *
     * @param width  Target wallpaper width in pixels
     * @param height Target wallpaper height in pixels
     */
    fun generateWallpaper(
        cells: List<DayCell>,
        stats: YearStats,
        accentColor: Int,
        width: Int = 1080,
        height: Int = 2400
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#101010"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // --- Safe zone calculation ---
        val topSafeZone = (height * 0.18f).toInt()   // Reserve for clock/date
        val bottomSafeZone = (height * 0.82f).toInt() // Reserve for notifications
        val gridAreaTop = topSafeZone
        val gridAreaBottom = bottomSafeZone
        val gridAreaHeight = gridAreaBottom - gridAreaTop
        val gridAreaLeft = (width * 0.05f).toInt()
        val gridAreaRight = (width * 0.95f).toInt()
        val gridAreaWidth = gridAreaRight - gridAreaLeft

        // --- Header text (Year) ---
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#F5F5F5")
        paint.textSize = (width * 0.10f)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(stats.year.toString(), gridAreaLeft.toFloat(), (topSafeZone - width * 0.04f), paint)

        // --- Stats text ---
        paint.textSize = (width * 0.038f)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = Color.parseColor("#A7A7A7")
        val statsText = "${stats.productiveDays} productive  ·  ${stats.formattedPercentage}"
        canvas.drawText(statsText, gridAreaLeft.toFloat(), (topSafeZone - width * 0.01f), paint)

        // --- Grid layout ---
        val columns = 25
        val rows = (cells.size + columns - 1) / columns

        val cellSize = minOf(
            gridAreaWidth.toFloat() / columns,
            gridAreaHeight.toFloat() / rows
        )
        val gap = cellSize * 0.18f
        val dotSize = (cellSize - gap) * 0.82f

        val totalGridWidth = columns * cellSize
        val totalGridHeight = rows * cellSize
        val gridLeft = gridAreaLeft + (gridAreaWidth - totalGridWidth) / 2f
        val gridTop = gridAreaTop + (gridAreaHeight - totalGridHeight) / 2f

        val today = LocalDate.now()

        cells.forEachIndexed { index, cell ->
            val col = index % columns
            val row = index / columns

            val cx = gridLeft + col * cellSize + cellSize / 2f
            val cy = gridTop + row * cellSize + cellSize / 2f
            val radius = dotSize / 2f

            drawCell(canvas, paint, cell, cx, cy, radius, accentColor)
        }

        // --- Bottom label ---
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.parseColor("#454545")
        paint.textSize = (width * 0.028f)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText(
            "daygrid · ${today.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
            width / 2f,
            (bottomSafeZone + height * 0.025f),
            paint
        )

        return bitmap
    }

    private fun drawCell(
        canvas: Canvas,
        paint: Paint,
        cell: DayCell,
        cx: Float,
        cy: Float,
        radius: Float,
        accentColor: Int
    ) {
        paint.style = Paint.Style.FILL

        when (cell.displayStatus) {
            DayCellDisplayStatus.PRODUCTIVE, DayCellDisplayStatus.TODAY_PRODUCTIVE -> {
                paint.color = accentColor
                canvas.drawCircle(cx, cy, radius, paint)
                if (cell.isToday) drawTodayRing(canvas, paint, cx, cy, radius, accentColor)
            }
            DayCellDisplayStatus.UNPRODUCTIVE, DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> {
                paint.color = Color.parseColor("#2A2A2A")
                canvas.drawCircle(cx, cy, radius, paint)
                if (cell.isToday) drawTodayRing(canvas, paint, cx, cy, radius, Color.parseColor("#606060"))
            }
            DayCellDisplayStatus.NEUTRAL, DayCellDisplayStatus.TODAY_NEUTRAL -> {
                paint.color = Color.parseColor("#3A3A3A")
                canvas.drawCircle(cx, cy, radius, paint)
                if (cell.isToday) drawTodayRing(canvas, paint, cx, cy, radius, Color.parseColor("#808080"))
            }
            DayCellDisplayStatus.TODAY_UNMARKED -> {
                paint.color = Color.parseColor("#242424")
                canvas.drawCircle(cx, cy, radius, paint)
                drawTodayRing(canvas, paint, cx, cy, radius, Color.parseColor("#A7A7A7"))
            }
            DayCellDisplayStatus.PAST_UNMARKED -> {
                paint.color = Color.parseColor("#1E1E1E")
                canvas.drawCircle(cx, cy, radius, paint)
            }
            DayCellDisplayStatus.FUTURE -> {
                paint.color = Color.parseColor("#171717")
                canvas.drawCircle(cx, cy, radius * 0.65f, paint)
            }
        }
    }

    private fun drawTodayRing(
        canvas: Canvas,
        paint: Paint,
        cx: Float,
        cy: Float,
        radius: Float,
        color: Int
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = radius * 0.25f
        canvas.drawCircle(cx, cy, radius + paint.strokeWidth * 0.6f, paint)
        paint.style = Paint.Style.FILL
    }

    /**
     * Applies the generated bitmap as the wallpaper.
     *
     * @param which WallpaperManager.FLAG_LOCK, FLAG_SYSTEM, or both combined.
     */
    fun applyWallpaper(bitmap: Bitmap, which: Int): Result<Unit> = runCatching {
        val wm = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wm.setBitmap(bitmap, null, true, which)
        } else {
            wm.setBitmap(bitmap)
        }
    }
}
