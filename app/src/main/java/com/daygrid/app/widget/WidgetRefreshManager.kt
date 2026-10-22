package com.daygrid.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefreshManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Triggers a refresh of all DayGrid widgets.
     * Called after a check-in or data change.
     *
     * Note: Widget updates are subject to Android launcher scheduling.
     * Updates may not be immediate depending on device battery optimization settings.
     */
    suspend fun refreshAll() {
        try {
            // Refresh compact widgets
            val compactManager = GlanceAppWidgetManager(context)
            val compactIds = compactManager.getGlanceIds(DayGridCompactWidget::class.java)
            compactIds.forEach { id ->
                DayGridCompactWidget().update(context, id)
            }

            // Refresh expanded widgets
            val expandedIds = compactManager.getGlanceIds(DayGridExpandedWidget::class.java)
            expandedIds.forEach { id ->
                DayGridExpandedWidget().update(context, id)
            }

            // Refresh Nothing lock screen widgets
            val nothingIds = compactManager.getGlanceIds(NothingScreenTimeWidget::class.java)
            nothingIds.forEach { id ->
                NothingScreenTimeWidget().update(context, id)
            }
        } catch (e: Exception) {
            // Widget refresh failure should never crash the main app flow
            e.printStackTrace()
        }
    }
}
