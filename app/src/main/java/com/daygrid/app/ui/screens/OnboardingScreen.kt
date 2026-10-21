package com.daygrid.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.daygrid.app.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.CalendarViewMonth,
            title = "See your year",
            subtitle = "Every day of the year, visible at a glance. One tiny dot — one day of your life."
        ),
        OnboardingPage(
            icon = Icons.Default.LightMode,
            title = "Reflect on your day",
            subtitle = "Each evening, take 10 seconds to mark whether you made today count. That's it."
        ),
        OnboardingPage(
            icon = Icons.Default.FavoriteBorder,
            title = "No pressure",
            subtitle = "No streaks to maintain. No guilt for rest. Just an honest record of your year."
        ),
        OnboardingPage(
            icon = Icons.Default.Lock,
            title = "Fully private",
            subtitle = "All data stays on your device. No accounts, no cloud, no tracking. Ever."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DayGridBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) DayGridOrange
                                else DayGridSurfaceVariant
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DayGridOrange)
            ) {
                Text(
                    if (isLastPage) "Get started" else "Continue",
                    color = DayGridBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (!isLastPage) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onComplete) {
                    Text("Skip", color = DayGridTextTertiary, fontSize = 14.sp)
                }
            }
        }
    }
}

private data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(DayGridSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = DayGridOrange,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DayGridTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            fontSize = 16.sp,
            color = DayGridTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(100.dp)) // clearance for bottom controls
    }
}
