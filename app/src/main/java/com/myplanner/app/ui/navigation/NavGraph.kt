package com.myplanner.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.local.PreferencesRepository
import com.myplanner.app.data.repository.ReminderRepository
import com.myplanner.app.ui.hello.HelloScreen
import com.myplanner.app.ui.home.HomeScreen
import com.myplanner.app.ui.reminder.CreateFirstReminderScreen
import com.myplanner.app.ui.setup.FirstReminderPromptScreen
import com.myplanner.app.ui.setup.NotificationSetupScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val HELLO = "hello"
    const val NOTIFICATION_SETUP = "notification_setup"
    const val FIRST_REMINDER_PROMPT = "first_reminder_prompt"
    const val CREATE_FIRST_REMINDER = "create_first_reminder"
    const val HOME = "home"
}

@Composable
fun MyPlannerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    preferencesRepository: PreferencesRepository,
    reminderRepository: ReminderRepository
) {
    val scope = rememberCoroutineScope()
    var startReady by remember { mutableStateOf(false) }
    var onboardingComplete by remember { mutableStateOf(false) }

    LaunchedEffect(preferencesRepository) {
        onboardingComplete = preferencesRepository.onboardingComplete.first()
        startReady = true
    }

    if (!startReady) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val startDestination = if (onboardingComplete) Routes.HOME else Routes.HELLO

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.HELLO) {
            HelloScreen(
                onGetStarted = {
                    navController.navigate(Routes.NOTIFICATION_SETUP) {
                        launchSingleTop = true
                    }
                },
                onExploreFirst = {
                    scope.launch {
                        preferencesRepository.completeOnboardingViaExplore()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HELLO) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.NOTIFICATION_SETUP) {
            BackHandler { navController.popBackStack() }
            NotificationSetupScreen(
                onContinue = { granted ->
                    scope.launch {
                        preferencesRepository.setNotificationsPrompted(true)
                        preferencesRepository.setNotificationsEnabled(granted)
                        navController.navigate(Routes.FIRST_REMINDER_PROMPT) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.FIRST_REMINDER_PROMPT) {
            BackHandler { navController.popBackStack() }
            FirstReminderPromptScreen(
                onCreateReminder = {
                    navController.navigate(Routes.CREATE_FIRST_REMINDER) {
                        launchSingleTop = true
                    }
                },
                onSkip = {
                    scope.launch {
                        val granted = preferencesRepository.notificationsEnabled.first()
                        preferencesRepository.completeOnboardingAfterSetup(granted)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HELLO) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Routes.CREATE_FIRST_REMINDER) {
            BackHandler { navController.popBackStack() }
            CreateFirstReminderScreen(
                onSave = { title, notes ->
                    reminderRepository.createReminder(title = title, notes = notes)
                    val granted = preferencesRepository.notificationsEnabled.first()
                    preferencesRepository.completeOnboardingAfterSetup(granted)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HELLO) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}

@Composable
fun MyPlannerApp(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember {
        PreferencesRepository(context.applicationContext)
    }
    val reminderRepository = remember {
        ReminderRepository(AppDatabase.getInstance(context).reminderDao())
    }

    MyPlannerNavGraph(
        modifier = modifier,
        preferencesRepository = preferencesRepository,
        reminderRepository = reminderRepository
    )
}
