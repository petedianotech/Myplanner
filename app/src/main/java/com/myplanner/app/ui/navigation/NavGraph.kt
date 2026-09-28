package com.myplanner.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myplanner.app.ui.foundation.DesignSystemScreen

/**
 * Navigation foundation for MyPlanner.
 * Phase 2 hosts the design-system gallery for visual validation.
 * Feature destinations will replace this start route in later phases.
 */
object Routes {
    const val DESIGN_SYSTEM = "design_system"
}

@Composable
fun MyPlannerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.DESIGN_SYSTEM
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.DESIGN_SYSTEM) {
            DesignSystemScreen()
        }
    }
}
