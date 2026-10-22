package com.daygrid.app

import android.Manifest
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
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
import com.daygrid.app.ui.theme.DayGridBackground
import com.daygrid.app.ui.theme.DayGridTextPrimary
import com.daygrid.app.ui.theme.DayGridTheme
import com.daygrid.app.ui.wallpaper.WallpaperViewModel
import com.daygrid.app.wallpaper.DayGridLiveWallpaperService
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val accentColor = remember(prefs.accentColor) {
        try {
            Color(android.graphics.Color.parseColor(prefs.accentColor))
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
                    Toast.makeText(
                        navController.context,
                        "${event.count} records imported",
                        Toast.LENGTH_SHORT
                    ).show()
                is SettingsEvent.Error ->
                    Toast.makeText(
                        navController.context, event.message, Toast.LENGTH_LONG
                    ).show()
                is SettingsEvent.DataDeleted ->
                    Toast.makeText(
                        navController.context, "All data deleted", Toast.LENGTH_SHORT
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
                    Toast.makeText(
                        navController.context, event.message, Toast.LENGTH_LONG
                    ).show()
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

    val isTopLevelDestination = currentRoute in listOf(
        Screen.YearGrid.route,
        Screen.Stats.route,
        Screen.Wallpaper.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = DayGridBackground,
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = DayGridBackground,
                    contentColor = DayGridTextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = Color(0xFF1E1E1E)
                    )
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.YearGrid.route,
                        onClick = {
                            if (currentRoute != Screen.YearGrid.route) {
                                navController.navigate(Screen.YearGrid.route) {
                                    popUpTo(Screen.YearGrid.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Grid") },
                        label = { Text("Grid", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor,
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575),
                            indicatorColor = accentColor.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Stats.route,
                        onClick = {
                            if (currentRoute != Screen.Stats.route) {
                                navController.navigate(Screen.Stats.route) {
                                    popUpTo(Screen.YearGrid.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                        label = { Text("Stats", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor,
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575),
                            indicatorColor = accentColor.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Wallpaper.route,
                        onClick = {
                            if (currentRoute != Screen.Wallpaper.route) {
                                navController.navigate(Screen.Wallpaper.route) {
                                    popUpTo(Screen.YearGrid.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Wallpaper, contentDescription = "Wallpaper") },
                        label = { Text("Wallpaper", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor,
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575),
                            indicatorColor = accentColor.copy(alpha = 0.12f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            if (currentRoute != Screen.Settings.route) {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(Screen.YearGrid.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor,
                            unselectedIconColor = Color(0xFF757575),
                            unselectedTextColor = Color(0xFF757575),
                            indicatorColor = accentColor.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        settingsViewModel.setOnboardingDone(true)
                        onRequestNotificationPermission()
                        navController.navigate(Screen.YearGrid.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
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

                val recordFlow = remember(date) {
                    gridViewModel.uiState.value.cells.find { it.date == date }?.record
                }
                var record by remember { mutableStateOf(recordFlow) }

                LaunchedEffect(date) {
                    gridViewModel.uiState.collect { state ->
                        record = state.cells.find { it.date == date }?.record
                    }
                }

                DateDetailScreen(
                    date = date,
                    record = record,
                    accentColor = accentColor,
                    onChangeStatus = { status ->
                        // Status modification handled in detail screen
                    },
                    onUpdateNote = { /* handled inline */ },
                    onDeleteRecord = {
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
                    cells = gridState.cells,
                    accentColor = accentColor,
                    onSetLiveWallpaper = {
                        try {
                            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                putExtra(
                                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                    ComponentName(navController.context, DayGridLiveWallpaperService::class.java)
                                )
                            }
                            navController.context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val chooser = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                                navController.context.startActivity(chooser)
                            } catch (e2: Exception) {
                                Toast.makeText(navController.context, "Open Android Settings > Wallpaper to set DayGrid Live Wallpaper", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
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
}
