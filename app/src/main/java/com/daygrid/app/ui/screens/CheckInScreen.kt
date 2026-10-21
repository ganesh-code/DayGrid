package com.daygrid.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daygrid.app.domain.model.DayStatus
import com.daygrid.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    date: LocalDate = LocalDate.now(),
    selectedStatus: DayStatus?,
    note: String,
    showReflection: Boolean,
    isSaving: Boolean,
    existingRecord: com.daygrid.app.domain.model.DayRecord?,
    accentColor: Color = DayGridOrange,
    onSelectStatus: (DayStatus) -> Unit,
    onUpdateNote: (String) -> Unit,
    onSave: () -> Unit,
    onSkipReflection: () -> Unit,
    onBack: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")

    Scaffold(
        containerColor = DayGridBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DayGridBackground),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DayGridTextSecondary)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Date label
            Text(
                text = date.format(formatter),
                fontSize = 14.sp,
                color = DayGridTextSecondary
            )

            Spacer(Modifier.height(24.dp))

            // Main question
            Text(
                text = "Did you make\ntoday count?",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = DayGridTextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Did you learn something, make progress toward a goal,\nhelp someone, or do something meaningful today?",
                fontSize = 14.sp,
                color = DayGridTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(40.dp))

            // Status choices
            CheckInOption(
                title = "Yes, I made progress",
                subtitle = "Mark today as productive",
                icon = Icons.Default.CheckCircle,
                isSelected = selectedStatus == DayStatus.PRODUCTIVE,
                selectedColor = accentColor,
                onClick = { onSelectStatus(DayStatus.PRODUCTIVE) },
                modifier = Modifier.semantics { contentDescription = "Mark today as productive" }
            )

            Spacer(Modifier.height(12.dp))

            CheckInOption(
                title = "Today was for rest",
                subtitle = "Rest is part of a sustainable life",
                icon = Icons.Default.Favorite,
                isSelected = selectedStatus == DayStatus.NEUTRAL,
                selectedColor = DayGridNeutralCell,
                onClick = { onSelectStatus(DayStatus.NEUTRAL) },
                modifier = Modifier.semantics { contentDescription = "Mark today as rest" }
            )

            Spacer(Modifier.height(12.dp))

            CheckInOption(
                title = "Not today",
                subtitle = "One day doesn't define your year",
                icon = Icons.Default.Close,
                isSelected = selectedStatus == DayStatus.UNPRODUCTIVE,
                selectedColor = DayGridTextSecondary,
                onClick = { onSelectStatus(DayStatus.UNPRODUCTIVE) },
                modifier = Modifier.semantics { contentDescription = "Mark today as not productive" }
            )

            // Reflection (optional, shown after status picked)
            AnimatedVisibility(
                visible = showReflection || selectedStatus != null,
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = DayGridSurfaceVariant)
                    Spacer(Modifier.height(20.dp))

                    Text(
                        "What did you do today?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DayGridTextPrimary
                    )
                    Text(
                        "Optional — keeps notes private and local.",
                        fontSize = 12.sp,
                        color = DayGridTextSecondary
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = onUpdateNote,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "e.g. Practised a new skill, completed an important task...",
                                color = DayGridTextTertiary,
                                fontSize = 13.sp
                            )
                        },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = DayGridSurfaceVariant,
                            focusedTextColor = DayGridTextPrimary,
                            unfocusedTextColor = DayGridTextPrimary,
                            cursorColor = accentColor
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSkipReflection,
                            modifier = Modifier.weight(1f),
                            enabled = selectedStatus != null && !isSaving,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DayGridTextSecondary)
                        ) {
                            Text("Skip & Save")
                        }

                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            enabled = selectedStatus != null && !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = DayGridBackground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save", color = DayGridBackground, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Save button when reflection not shown yet
            if (!showReflection && selectedStatus != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DayGridBackground,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save", color = DayGridBackground, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun CheckInOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) selectedColor else DayGridSurfaceVariant
    val bgColor = if (isSelected) selectedColor.copy(alpha = 0.12f) else DayGridSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedColor else DayGridTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (isSelected) DayGridTextPrimary else DayGridTextSecondary
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = DayGridTextTertiary
            )
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = selectedColor, modifier = Modifier.size(18.dp))
        }
    }
}
