package com.daygrid.app.ui.wallpaper

import android.app.WallpaperManager
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.domain.usecase.GetYearGridUseCase
import com.daygrid.app.domain.usecase.GetYearStatsUseCase
import com.daygrid.app.wallpaper.WallpaperGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class WallpaperEvent {
    data class GenerateSuccess(val bitmap: android.graphics.Bitmap) : WallpaperEvent()
    data class ApplySuccess(val target: String) : WallpaperEvent()
    data class Error(val message: String) : WallpaperEvent()
}

data class WallpaperUiState(
    val isGenerating: Boolean = false,
    val isApplying: Boolean = false,
    val previewBitmap: android.graphics.Bitmap? = null
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val getYearGrid: GetYearGridUseCase,
    private val getYearStats: GetYearStatsUseCase,
    private val wallpaperGenerator: WallpaperGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperUiState())
    val uiState: StateFlow<WallpaperUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WallpaperEvent>()
    val events: SharedFlow<WallpaperEvent> = _events.asSharedFlow()

    fun generateWallpaper(year: Int = LocalDate.now().year, accentColorHex: String = "#FF8A00") {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val cells = getYearGrid(year).first()
                val stats = getYearStats(year).first()
                val accentColor = Color.parseColor(accentColorHex)

                val bitmap = wallpaperGenerator.generateWallpaper(
                    cells = cells,
                    stats = stats,
                    accentColor = accentColor
                )
                _uiState.update { it.copy(isGenerating = false, previewBitmap = bitmap) }
                _events.emit(WallpaperEvent.GenerateSuccess(bitmap))
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false) }
                _events.emit(WallpaperEvent.Error(e.message ?: "Generation failed"))
            }
        }
    }

    fun applyWallpaper(which: Int) {
        val bitmap = _uiState.value.previewBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            wallpaperGenerator.applyWallpaper(bitmap, which).fold(
                onSuccess = {
                    _uiState.update { it.copy(isApplying = false) }
                    val label = when (which) {
                        WallpaperManager.FLAG_LOCK -> "lock screen"
                        WallpaperManager.FLAG_SYSTEM -> "home screen"
                        else -> "home & lock screen"
                    }
                    _events.emit(WallpaperEvent.ApplySuccess(label))
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isApplying = false) }
                    _events.emit(WallpaperEvent.Error(e.message ?: "Failed to apply wallpaper"))
                }
            )
        }
    }
}
