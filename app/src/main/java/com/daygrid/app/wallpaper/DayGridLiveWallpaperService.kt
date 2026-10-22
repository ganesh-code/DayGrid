package com.daygrid.app.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.DayCellDisplayStatus
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.widget.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DayGridLiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = DayGridEngine()

    inner class DayGridEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var isVisible = false
        private var accentColorInt = Color.parseColor("#FF8A00")
        private var cachedCells: List<DayCell> = emptyList()
        private var cachedStats: YearStats? = null

        private val timeTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                drawFrame()
            }
        }

        private val drawRunnable = Runnable {
            drawFrame()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            loadData()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            }
            try {
                registerReceiver(timeTickReceiver, filter)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                unregisterReceiver(timeTickReceiver)
            } catch (e: Exception) {
                // Ignore receiver error
            }
            scope.cancel()
            handler.removeCallbacks(drawRunnable)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                loadData()
                drawFrame()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        private fun loadData() {
            scope.launch(Dispatchers.IO) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        applicationContext,
                        WidgetEntryPoint::class.java
                    )
                    val year = LocalDate.now().year
                    val cells = entryPoint.getYearGridUseCase()(year).first()
                    val stats = entryPoint.getYearStatsUseCase()(year).first()
                    cachedCells = cells
                    cachedStats = stats

                    val fastPrefs = applicationContext.getSharedPreferences("daygrid_fast_prefs", Context.MODE_PRIVATE)
                    val colorHex = fastPrefs.getString("accent_color", "#FF8A00") ?: "#FF8A00"
                    accentColorInt = try { Color.parseColor(colorHex) } catch (e: Exception) { Color.parseColor("#FF8A00") }

                    withContext(Dispatchers.Main) {
                        drawFrame()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderLiveWallpaper(canvas)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        // Ignore unlock errors
                    }
                }
            }
        }

        private fun renderLiveWallpaper(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()

            // True OLED black background
            canvas.drawColor(Color.parseColor("#000000"))

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // --- Centered Pure Year Grid (No time, no date, no text headers) ---
            // Centered vertically in middle 60% of the screen
            val gridAreaTop = height * 0.22f
            val gridAreaBottom = height * 0.78f
            val gridAreaHeight = gridAreaBottom - gridAreaTop
            val gridAreaLeft = width * 0.06f
            val gridAreaRight = width * 0.94f
            val gridAreaWidth = gridAreaRight - gridAreaLeft

            val columns = 25
            val cells = cachedCells
            if (cells.isNotEmpty()) {
                val rows = (cells.size + columns - 1) / columns
                val cellSize = minOf(gridAreaWidth / columns, gridAreaHeight / rows)
                val gap = cellSize * 0.22f
                val dotRadius = (cellSize - gap) / 2f

                val totalGridWidth = columns * cellSize
                val totalGridHeight = rows * cellSize
                val startX = gridAreaLeft + (gridAreaWidth - totalGridWidth) / 2f
                val startY = gridAreaTop + (gridAreaHeight - totalGridHeight) / 2f

                cells.forEachIndexed { index, cell ->
                    val col = index % columns
                    val row = index / columns
                    val cx = startX + col * cellSize + cellSize / 2f
                    val cy = startY + row * cellSize + cellSize / 2f

                    paint.style = Paint.Style.FILL
                    when (cell.displayStatus) {
                        DayCellDisplayStatus.PRODUCTIVE, DayCellDisplayStatus.TODAY_PRODUCTIVE -> {
                            paint.color = accentColorInt
                            canvas.drawCircle(cx, cy, dotRadius, paint)
                        }
                        DayCellDisplayStatus.UNPRODUCTIVE, DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> {
                            paint.color = Color.parseColor("#262626")
                            canvas.drawCircle(cx, cy, dotRadius, paint)
                        }
                        DayCellDisplayStatus.NEUTRAL, DayCellDisplayStatus.TODAY_NEUTRAL -> {
                            paint.color = Color.parseColor("#383838")
                            canvas.drawCircle(cx, cy, dotRadius, paint)
                        }
                        DayCellDisplayStatus.TODAY_UNMARKED -> {
                            paint.color = Color.parseColor("#484848")
                            canvas.drawCircle(cx, cy, dotRadius, paint)
                        }
                        DayCellDisplayStatus.PAST_UNMARKED -> {
                            paint.color = Color.parseColor("#1E1E1E")
                            canvas.drawCircle(cx, cy, dotRadius, paint)
                        }
                        DayCellDisplayStatus.FUTURE -> {
                            paint.color = Color.parseColor("#121212")
                            canvas.drawCircle(cx, cy, dotRadius, paint)
                        }
                    }

                    if (cell.isToday) {
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = dotRadius * 0.5f
                        paint.color = Color.WHITE
                        canvas.drawCircle(cx, cy, dotRadius + gap * 0.45f, paint)
                    }
                }
            }
        }
    }
}
