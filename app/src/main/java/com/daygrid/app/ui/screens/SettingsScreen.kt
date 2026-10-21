package com.daygrid.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.data.preferences.UserPreferences
import com.daygrid.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit,
    onAccentColorChange: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDeleteAll: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = { Text("Settings", color = DayGridTextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DayGridTextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Notifications ──────────────────────────────
            SectionHeader("Notifications")

            SettingsToggleRow(
                title = "Evening reminder",
                subtitle = "Receive a daily check-in prompt",
                checked = preferences.reminderEnabled,
                onCheckedChange = onReminderToggle
            )

            AnimatedVisibility(visible = preferences.reminderEnabled) {
                SettingsClickRow(
                    title = "Reminder time",
                    value = "%02d:%02d".format(preferences.reminderHour, preferences.reminderMinute),
                    onClick = { showTimePicker = true }
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Android battery or notification settings may affect delivery timing.",
                fontSize = 11.sp,
                color = DayGridTextTertiary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Appearance ──────────────────────────────────
            SectionHeader("Appearance")

            Text("Accent colour", fontSize = 14.sp, color = DayGridTextSecondary, modifier = Modifier.padding(vertical = 8.dp))

            AccentColorPicker(
                currentColor = preferences.accentColor,
                onColorSelected = onAccentColorChange
            )

            Spacer(Modifier.height(24.dp))

            // ── Data ──────────────────────────────────────
            SectionHeader("Data")

            SettingsClickRow(
                title = "Export data",
                subtitle = "Save all records as JSON",
                icon = Icons.Default.Upload,
                onClick = onExport
            )

            SettingsClickRow(
                title = "Import data",
                subtitle = "Load records from JSON file",
                icon = Icons.Default.Download,
                onClick = onImport
            )

            Spacer(Modifier.height(8.dp))

            Surface(color = DayGridSurface, shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Delete all data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        Text("Permanently removes all records", fontSize = 12.sp, color = DayGridTextTertiary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── About ──────────────────────────────────────
            SectionHeader("About")
            Surface(color = DayGridSurface, shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DayGrid v1.0.0", color = DayGridTextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Privacy-first. All data stored locally. No accounts, no cloud, no tracking.", fontSize = 12.sp, color = DayGridTextSecondary, lineHeight = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Min Android: API 33 (Android 13)", fontSize = 11.sp, color = DayGridTextTertiary)
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = preferences.reminderHour,
            initialMinute = preferences.reminderMinute,
            onTimeSelected = { h, m ->
                showTimePicker = false
                onReminderTimeChange(h, m)
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = DayGridSurface,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all data?", color = DayGridTextPrimary) },
            text = {
                Text(
                    "This permanently deletes all your day records. This action cannot be undone.",
                    color = DayGridTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAll()
                }) {
                    Text("Delete everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = DayGridTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = DayGridTextTertiary,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(color = DayGridSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = DayGridTextPrimary, fontWeight = FontWeight.Medium)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = DayGridTextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = DayGridBackground, checkedTrackColor = DayGridOrange)
            )
        }
    }
}

@Composable
private fun SettingsClickRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        color = DayGridSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = DayGridTextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = DayGridTextPrimary, fontWeight = FontWeight.Medium)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = DayGridTextSecondary)
            }
            if (value != null) {
                Text(value, color = DayGridOrange, fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DayGridTextTertiary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour.coerceIn(20, 21),
        initialMinute = initialMinute
    )

    AlertDialog(
        containerColor = DayGridSurface,
        onDismissRequest = onDismiss,
        title = { Text("Set reminder time", color = DayGridTextPrimary) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = DayGridOrange,
                        timeSelectorSelectedContentColor = DayGridBackground
                    )
                )
                Text(
                    "Note: reminder time is between 8:00 PM and 9:00 PM",
                    fontSize = 11.sp,
                    color = DayGridTextTertiary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = state.hour.coerceIn(20, 21)
                onTimeSelected(h, state.minute)
            }) { Text("Set", color = DayGridOrange) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = DayGridTextSecondary) }
        }
    )
}

@Composable
private fun AccentColorPicker(
    currentColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FF8A00" to "Orange",
        "#4FC3F7" to "Sky",
        "#81C784" to "Mint",
        "#CE93D8" to "Lavender",
        "#EF9A9A" to "Rose",
        "#FFD54F" to "Gold"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEach { (hex, name) ->
            val color = Color(android.graphics.Color.parseColor(hex))
            val isSelected = hex == currentColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.background(color)
                        else Modifier
                    )
                    .clickable { onColorSelected(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "$name selected",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
