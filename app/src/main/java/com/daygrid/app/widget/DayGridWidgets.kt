package com.daygrid.app.widget

import android.content.Context
import android.content.Intent
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
import com.daygrid.app.domain.model.DayCellDisplayStatus
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.usecase.GetYearGridUseCase
import com.daygrid.app.domain.usecase.GetYearStatsUseCase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate

// ─────────────────────────────────────────
// Entry point for Glance widget (Hilt can't inject directly into GlanceAppWidget)
// ─────────────────────────────────────────
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getYearGridUseCase(): GetYearGridUseCase
    fun getYearStatsUseCase(): GetYearStatsUseCase
}

// ─────────────────────────────────────────
// Compact Widget
// ─────────────────────────────────────────
class DayGridCompactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val year = LocalDate.now().year
        val cells = entryPoint.getYearGridUseCase()(year).first()
        val stats = entryPoint.getYearStatsUseCase()(year).first()

        provideContent {
            WidgetContent(
                year = year,
                cells = cells,
                productiveDays = stats.productiveDays,
                percentage = stats.formattedPercentage,
                isExpanded = false,
                context = context
            )
        }
    }
}

class DayGridCompactWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayGridCompactWidget()
}

// ─────────────────────────────────────────
// Expanded Widget
// ─────────────────────────────────────────
class DayGridExpandedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val year = LocalDate.now().year
        val cells = entryPoint.getYearGridUseCase()(year).first()
        val stats = entryPoint.getYearStatsUseCase()(year).first()

        provideContent {
            WidgetContent(
                year = year,
                cells = cells,
                productiveDays = stats.productiveDays,
                percentage = stats.formattedPercentage,
                isExpanded = true,
                context = context
            )
        }
    }
}

class DayGridExpandedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayGridExpandedWidget()
}

// ─────────────────────────────────────────
// Shared Widget UI (Glance Composable)
// ─────────────────────────────────────────
@Composable
private fun WidgetContent(
    year: Int,
    cells: List<com.daygrid.app.domain.model.DayCell>,
    productiveDays: Int,
    percentage: String,
    isExpanded: Boolean,
    context: Context
) {
    val mainIntent = Intent(context, MainActivity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF101010)))
            .clickable(actionStartActivity(mainIntent))
            .padding(8.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // Header row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                Text(
                    text = "$year",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF5F5F5)),
                        fontSize = if (isExpanded) 16.sp else 13.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = if (isExpanded) "$productiveDays · $percentage" else "$productiveDays",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFA7A7A7)),
                        fontSize = if (isExpanded) 12.sp else 11.sp
                    )
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            // Mini grid
            MiniGrid(cells = cells, isExpanded = isExpanded)
        }
    }
}

@Composable
private fun MiniGrid(
    cells: List<com.daygrid.app.domain.model.DayCell>,
    isExpanded: Boolean
) {
    val columns = if (isExpanded) 25 else 20
    val dotDp = if (isExpanded) 5.dp else 4.dp
    val gapDp = if (isExpanded) 1.dp else 1.dp

    Column {
        var index = 0
        while (index < cells.size) {
            Row {
                for (col in 0 until columns) {
                    if (index >= cells.size) break
                    val cell = cells[index]
                    val color = when (cell.displayStatus) {
                        DayCellDisplayStatus.PRODUCTIVE,
                        DayCellDisplayStatus.TODAY_PRODUCTIVE -> Color(0xFFFF8A00)
                        DayCellDisplayStatus.UNPRODUCTIVE,
                        DayCellDisplayStatus.TODAY_UNPRODUCTIVE -> Color(0xFF2A2A2A)
                        DayCellDisplayStatus.NEUTRAL,
                        DayCellDisplayStatus.TODAY_NEUTRAL -> Color(0xFF3A3A3A)
                        DayCellDisplayStatus.TODAY_UNMARKED -> Color(0xFF454545)
                        DayCellDisplayStatus.PAST_UNMARKED -> Color(0xFF1E1E1E)
                        DayCellDisplayStatus.FUTURE -> Color(0xFF141414)
                    }
                    Box(
                        modifier = GlanceModifier
                            .size(dotDp)
                            .background(ColorProvider(color))
                    ) {}
                    if (col < columns - 1) Spacer(GlanceModifier.width(gapDp))
                    index++
                }
            }
            Spacer(GlanceModifier.height(gapDp))
        }
    }
}
