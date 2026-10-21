package com.daygrid.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import java.time.LocalDate

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object YearGrid : Screen("year_grid")
    object CheckIn : Screen("check_in/{date}") {
        fun createRoute(date: LocalDate = LocalDate.now()) = "check_in/${date}"
    }
    object DateDetail : Screen("date_detail/{date}") {
        fun createRoute(date: LocalDate) = "date_detail/${date}"
    }
    object Stats : Screen("stats")
    object Wallpaper : Screen("wallpaper")
    object Settings : Screen("settings")
}
