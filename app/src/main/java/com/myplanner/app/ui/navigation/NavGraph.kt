package com.myplanner.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myplanner.app.ui.foundation.FoundationScreen

/**
 * Navigation foundation for MyPlanner.
 * Phase 1 only hosts the design-system validation screen.
 * Future destinations will be added here.
 */
object Routes {
    const val FOUNDATION = "foundation"
}

@Composable
fun MyPlannerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.FOUNDATION
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.FOUNDATION) {
            FoundationScreen()
        }
    }
}
