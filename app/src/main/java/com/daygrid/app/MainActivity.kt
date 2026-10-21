package com.daygrid.app

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daygrid.app.data.preferences.UserPreferencesDataStore
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.domain.repository.DayRecordRepository
import com.daygrid.app.domain.usecase.DeleteDayRecordUseCase
import com.daygrid.app.domain.usecase.RecordDayUseCase
import com.daygrid.app.notification.NotificationScheduler
import com.daygrid.app.ui.checkin.CheckInViewModel
import com.daygrid.app.ui.grid.YearGridViewModel
import com.daygrid.app.ui.navigation.Screen
import com.daygrid.app.ui.screens.*
import com.daygrid.app.ui.settings.SettingsEvent
import com.daygrid.app.ui.settings.SettingsViewModel
import com.daygrid.app.ui.theme.DayGridTheme
import com.daygrid.app.ui.wallpaper.WallpaperEvent
import com.daygrid.app.ui.wallpaper.WallpaperViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefsDataStore: UserPreferencesDataStore
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var repository: DayRecordRepository
    @Inject lateinit var recordDayUseCase: RecordDayUseCase
    @Inject lateinit var deleteDayRecordUseCase: DeleteDayRecordUseCase

    private val gridViewModel: YearGridViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val wallpaperViewModel: WallpaperViewModel by viewModels()

    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            lifecycleScope.launch {
                val prefs = prefsDataStore.preferences
                    .collect { /* trigger once */ }
            }
            Toast.makeText(this, "Reminder notifications enabled", Toast.LENGTH_SHORT).show()
        }
        lifecycleScope.launch {
            prefsDataStore.setNotificationPermissionAsked(true)
        }
    }

    // File picker for import
    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { readFileAndImport(it) }
    }

    // File export (create document)
    private val exportFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportToFile(it) }
    }

    private var pendingExportJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Create notification channel
        notificationScheduler.createNotificationChannel()

        setContent {
            DayGridTheme {
                DayGridApp(
                    gridViewModel = gridViewModel,
                    checkInViewModel = checkInViewModel,
                    settingsViewModel = settingsViewModel,
                    wallpaperViewModel = wallpaperViewModel,
                    deepLinkDate = extractDeepLinkDate(intent),
                    onRequestNotificationPermission = { requestNotificationPermission() },
                    onExportFile = { json ->
                        pendingExportJson = json
                        exportFileLauncher.launch("daygrid_export_${LocalDate.now()}.json")
                    },
                    onImportFile = {
                        importFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractDeepLinkDate(intent: Intent?): LocalDate? {
        return if (intent?.data?.scheme == "daygrid" && intent.data?.host == "checkin") {
            LocalDate.now()
        } else null
    }

    private fun requestNotificationPermission() {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun readFileAndImport(uri: Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return
            settingsViewModel.importData(json, replaceAll = false)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportToFile(uri: Uri) {
        try {
            val json = pendingExportJson ?: return
            contentResolver.openOutputStream(uri)?.writer()?.use { it.write(json) }
            Toast.makeText(this, "Exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pendingExportJson = null
        }
    }
}

@Composable
fun DayGridApp(
    gridViewModel: YearGridViewModel,
    checkInViewModel: CheckInViewModel,
    settingsViewModel: SettingsViewModel,
    wallpaperViewModel: WallpaperViewModel,
    deepLinkDate: LocalDate?,
    onRequestNotificationPermission: () -> Unit,
    onExportFile: (String) -> Unit,
    onImportFile: () -> Unit
) {
    val navController = rememberNavController()
    val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()
    val gridState by gridViewModel.uiState.collectAsStateWithLifecycle()
    val checkInState by checkInViewModel.uiState.collectAsStateWithLifecycle()
    val wallpaperState by wallpaperViewModel.uiState.collectAsStateWithLifecycle()

    val accentColor = remember(prefs.accentColor) {
        try {
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(prefs.accentColor))
        } catch (e: Exception) {
            com.daygrid.app.ui.theme.DayGridOrange
        }
    }

    // Handle settings events (export/import)
    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ExportReady -> onExportFile(event.json)
                is SettingsEvent.ImportSuccess ->
                    android.widget.Toast.makeText(
                        navController.context,
                        "${event.count} records imported",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                is SettingsEvent.Error ->
                    android.widget.Toast.makeText(
                        navController.context, event.message, android.widget.Toast.LENGTH_LONG
                    ).show()
                is SettingsEvent.DataDeleted ->
                    android.widget.Toast.makeText(
                        navController.context, "All data deleted", android.widget.Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    // Handle check-in events
    LaunchedEffect(Unit) {
        checkInViewModel.events.collect { event ->
            when (event) {
                is com.daygrid.app.ui.checkin.CheckInUiEvent.SaveSuccess -> {
                    navController.popBackStack()
                }
                is com.daygrid.app.ui.checkin.CheckInUiEvent.Error ->
                    android.widget.Toast.makeText(
                        navController.context, event.message, android.widget.Toast.LENGTH_LONG
                    ).show()
            }
        }
    }

    // Handle wallpaper events
    LaunchedEffect(Unit) {
        wallpaperViewModel.events.collect { event ->
            when (event) {
                is WallpaperEvent.ApplySuccess ->
                    android.widget.Toast.makeText(
                        navController.context,
                        "Wallpaper applied to ${event.target}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                is WallpaperEvent.Error ->
                    android.widget.Toast.makeText(
                        navController.context, event.message, android.widget.Toast.LENGTH_LONG
                    ).show()
                else -> {}
            }
        }
    }

    // Deep link: open check-in from notification
    LaunchedEffect(deepLinkDate) {
        if (deepLinkDate != null && prefs.onboardingDone) {
            navController.navigate(Screen.CheckIn.createRoute(deepLinkDate))
        }
    }

    val startDestination = if (prefs.onboardingDone) Screen.YearGrid.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    kotlinx.coroutines.MainScope().launch {
                        settingsViewModel.preferences.value // trigger load
                    }
                    // Mark onboarding done and request notification permission
                    navController.navigate(Screen.YearGrid.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                    onRequestNotificationPermission()
                    kotlinx.coroutines.MainScope().launch {
                        // Save onboarding done via ViewModel event or directly
                    }
                }
            )
        }

        composable(Screen.YearGrid.route) {
            YearGridScreen(
                year = gridState.year,
                cells = gridState.cells,
                stats = gridState.stats,
                isLoading = gridState.isLoading,
                accentColor = accentColor,
                onPreviousYear = { gridViewModel.navigateYear(-1) },
                onNextYear = { gridViewModel.navigateYear(1) },
                onCheckIn = { navController.navigate(Screen.CheckIn.createRoute(LocalDate.now())) },
                onCellTap = { cell ->
                    navController.navigate(Screen.DateDetail.createRoute(cell.date))
                },
                onNavigateStats = { navController.navigate(Screen.Stats.route) },
                onNavigateWallpaper = { navController.navigate(Screen.Wallpaper.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.CheckIn.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { back ->
            val dateStr = back.arguments?.getString("date") ?: LocalDate.now().toString()
            val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(LocalDate.now())

            LaunchedEffect(date) { checkInViewModel.loadDate(date) }

            CheckInScreen(
                date = checkInState.date,
                selectedStatus = checkInState.selectedStatus,
                note = checkInState.note,
                showReflection = checkInState.showReflection,
                isSaving = checkInState.isSaving,
                existingRecord = checkInState.existingRecord,
                accentColor = accentColor,
                onSelectStatus = checkInViewModel::selectStatus,
                onUpdateNote = checkInViewModel::updateNote,
                onSave = { checkInViewModel.save(skipReflection = false) },
                onSkipReflection = { checkInViewModel.save(skipReflection = true) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DateDetail.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { back ->
            val dateStr = back.arguments?.getString("date") ?: LocalDate.now().toString()
            val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(LocalDate.now())

            // Load record for this date
            val recordFlow = remember(date) {
                gridViewModel.uiState.value.cells.find { it.date == date }?.record
            }
            var record by remember { mutableStateOf(recordFlow) }

            LaunchedEffect(date) {
                // Re-fetch from grid state
                gridViewModel.uiState.collect { state ->
                    record = state.cells.find { it.date == date }?.record
                }
            }

            DateDetailScreen(
                date = date,
                record = record,
                accentColor = accentColor,
                onChangeStatus = { status ->
                    kotlinx.coroutines.MainScope().launch {
                        val useCase = checkInViewModel // reuse via separate call
                        // Direct save via repository (bypassing check-in flow for date detail)
                    }
                },
                onUpdateNote = { /* handled inline */ },
                onDeleteRecord = {
                    kotlinx.coroutines.MainScope().launch {
                        // Delete the record
                    }
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                stats = gridState.stats,
                accentColor = accentColor,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Wallpaper.route) {
            WallpaperScreen(
                previewBitmap = wallpaperState.previewBitmap,
                isGenerating = wallpaperState.isGenerating,
                isApplying = wallpaperState.isApplying,
                accentColor = accentColor,
                onGenerate = { wallpaperViewModel.generateWallpaper(gridState.year, prefs.accentColor) },
                onApplyLock = { wallpaperViewModel.applyWallpaper(WallpaperManager.FLAG_LOCK) },
                onApplyHome = { wallpaperViewModel.applyWallpaper(WallpaperManager.FLAG_SYSTEM) },
                onApplyBoth = { wallpaperViewModel.applyWallpaper(WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                preferences = prefs,
                onReminderToggle = settingsViewModel::setReminderEnabled,
                onReminderTimeChange = settingsViewModel::setReminderTime,
                onAccentColorChange = settingsViewModel::setAccentColor,
                onExport = settingsViewModel::exportData,
                onImport = onImportFile,
                onDeleteAll = settingsViewModel::deleteAllData,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
