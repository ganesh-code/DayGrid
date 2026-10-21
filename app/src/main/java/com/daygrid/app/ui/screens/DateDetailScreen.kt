package com.daygrid.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.domain.model.DayRecord
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDetailScreen(
    date: LocalDate,
    record: DayRecord?,
    onChangeStatus: (DayStatus) -> Unit,
    onUpdateNote: (String) -> Unit,
    onDeleteRecord: () -> Unit,
    onBack: () -> Unit,
    accentColor: Color = DayGridOrange
) {
    val longFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editNote by remember(record) { mutableStateOf(record?.note ?: "") }

    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = {
                    Text(
                        date.format(longFormatter),
                        color = DayGridTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DayGridTextSecondary)
                    }
                },
                actions = {
                    if (record != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove record", tint = DayGridTextSecondary)
                        }
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
            Spacer(Modifier.height(12.dp))

            // Status indicator
            val statusColor = when (record?.status) {
                DayStatus.PRODUCTIVE -> accentColor
                DayStatus.UNPRODUCTIVE -> DayGridUnproductive
                DayStatus.NEUTRAL -> DayGridNeutralMark
                null -> DayGridPastUnmarked
            }
            val statusLabel = when (record?.status) {
                DayStatus.PRODUCTIVE -> "Productive"
                DayStatus.UNPRODUCTIVE -> "Not productive"
                DayStatus.NEUTRAL -> "Rest"
                null -> "Unmarked"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(statusColor, CircleShape)
                )
                Text(statusLabel, color = DayGridTextSecondary, fontSize = 15.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Change status",
                fontSize = 13.sp,
                color = DayGridTextTertiary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            // Future check
            val today = LocalDate.now()
            val isFuture = date.isAfter(today)

            if (isFuture) {
                Surface(color = DayGridSurface, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "Future dates cannot be marked.",
                        modifier = Modifier.padding(16.dp),
                        color = DayGridTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusButton(
                        label = "Productive",
                        isSelected = record?.status == DayStatus.PRODUCTIVE,
                        selectedColor = accentColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onChangeStatus(DayStatus.PRODUCTIVE) }
                    )
                    StatusButton(
                        label = "Rest",
                        isSelected = record?.status == DayStatus.NEUTRAL,
                        selectedColor = DayGridNeutralCell,
                        modifier = Modifier.weight(1f),
                        onClick = { onChangeStatus(DayStatus.NEUTRAL) }
                    )
                    StatusButton(
                        label = "Not today",
                        isSelected = record?.status == DayStatus.UNPRODUCTIVE,
                        selectedColor = DayGridTextSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onChangeStatus(DayStatus.UNPRODUCTIVE) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!isFuture) {
                Text(
                    "Note",
                    fontSize = 13.sp,
                    color = DayGridTextTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editNote,
                    onValueChange = {
                        editNote = it
                        onUpdateNote(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Add a private note...",
                            color = DayGridTextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = DayGridSurfaceVariant,
                        focusedTextColor = DayGridTextPrimary,
                        unfocusedTextColor = DayGridTextPrimary,
                        cursorColor = accentColor
                    )
                )
            }

            if (record != null) {
                Spacer(Modifier.height(8.dp))
                val updatedAt = java.util.Date(record.updatedAt)
                Text(
                    "Last updated: $updatedAt",
                    fontSize = 11.sp,
                    color = DayGridTextTertiary
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            containerColor = DayGridSurface,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove record?", color = DayGridTextPrimary) },
            text = {
                Text(
                    "This will remove the day's status, returning it to unmarked. The day will not be counted.",
                    color = DayGridTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteRecord()
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
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
private fun StatusButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(1.5.dp, selectedColor, RoundedCornerShape(10.dp))
                else Modifier.border(1.dp, DayGridSurfaceVariant, RoundedCornerShape(10.dp))
            ),
        color = if (isSelected) selectedColor.copy(alpha = 0.15f) else DayGridSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = if (isSelected) DayGridTextPrimary else DayGridTextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
