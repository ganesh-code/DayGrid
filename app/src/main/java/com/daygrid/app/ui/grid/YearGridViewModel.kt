package com.daygrid.app.ui.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daygrid.app.data.preferences.UserPreferencesDataStore
import com.daygrid.app.domain.model.DayCell
import com.daygrid.app.domain.model.YearStats
import com.daygrid.app.domain.usecase.GetYearGridUseCase
import com.daygrid.app.domain.usecase.GetYearStatsUseCase
import com.daygrid.app.widget.WidgetRefreshManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class YearGridUiState(
    val year: Int = LocalDate.now().year,
    val cells: List<DayCell> = emptyList(),
    val stats: YearStats? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class YearGridViewModel @Inject constructor(
    private val getYearGrid: GetYearGridUseCase,
    private val getYearStats: GetYearStatsUseCase,
    private val prefsDataStore: UserPreferencesDataStore,
    private val widgetRefreshManager: WidgetRefreshManager
) : ViewModel() {

    private val _year = MutableStateFlow(LocalDate.now().year)
    val year: StateFlow<Int> = _year.asStateFlow()

    val uiState: StateFlow<YearGridUiState> = _year.flatMapLatest { year ->
        combine(
            getYearGrid(year),
            getYearStats(year)
        ) { cells, stats ->
            YearGridUiState(
                year = year,
                cells = cells,
                stats = stats,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YearGridUiState(isLoading = true)
    )

    fun navigateYear(delta: Int) {
        _year.value = (_year.value + delta).coerceIn(2000, 2100)
    }

    fun refreshWidgets() {
        viewModelScope.launch {
            widgetRefreshManager.refreshAll()
        }
    }
}
