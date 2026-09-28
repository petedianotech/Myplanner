package com.myplanner.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myplanner.app.data.local.PreferencesRepository
import com.myplanner.app.ui.capture.CaptureType
import com.myplanner.app.ui.capture.QuickCaptureSheet
import com.myplanner.app.ui.hello.HelloScreen
import com.myplanner.app.ui.home.HomeScreen
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.idea.CreateIdeaScreen
import com.myplanner.app.ui.note.CreateNoteScreen
import com.myplanner.app.ui.plans.IdeasScreen
import com.myplanner.app.ui.plans.NotesScreen
import com.myplanner.app.ui.plans.PlansScreen
import com.myplanner.app.ui.reminder.CreateFirstReminderScreen
import com.myplanner.app.ui.reminder.CreateReminderScreen
import com.myplanner.app.ui.setup.FirstReminderPromptScreen
import com.myplanner.app.ui.setup.NotificationSetupScreen
import com.myplanner.app.ui.task.CreateTaskScreen
import com.myplanner.app.ui.voice.VoiceNoteScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val HELLO = "hello"
    const val NOTIFICATION_SETUP = "notification_setup"
    const val FIRST_REMINDER_PROMPT = "first_reminder_prompt"
    const val CREATE_FIRST_REMINDER = "create_first_reminder"
    const val MAIN = "main"
    const val HOME = "home"
    const val PLANS = "plans"
    const val NOTES = "notes"
    const val IDEAS = "ideas"
    const val CREATE_TASK = "create_task"
    const val CREATE_REMINDER = "create_reminder"
    const val CREATE_NOTE = "create_note"
    const val CREATE_IDEA = "create_idea"
    const val CREATE_VOICE = "create_voice"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MyPlannerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    preferencesRepository: PreferencesRepository
) {
    val scope = rememberCoroutineScope()
    var startReady by remember { mutableStateOf(false) }
    var onboardingComplete by remember { mutableStateOf(false) }
    LaunchedEffect(preferencesRepository) {
        onboardingComplete = preferencesRepository.onboardingComplete.first()
        startReady = true
    }
    if (!startReady) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }
    val startDestination = if (onboardingComplete) Routes.MAIN else Routes.HELLO
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.HELLO) {
            HelloScreen(
                onGetStarted = { navController.navigate(Routes.NOTIFICATION_SETUP) { launchSingleTop = true } },
                onExploreFirst = {
                    scope.launch {
                        preferencesRepository.completeOnboardingViaExplore()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.HELLO) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.NOTIFICATION_SETUP) {
            BackHandler { navController.popBackStack() }
            NotificationSetupScreen(onContinue = { granted ->
                scope.launch {
                    preferencesRepository.setNotificationsPrompted(true)
                    preferencesRepository.setNotificationsEnabled(granted)
                    navController.navigate(Routes.FIRST_REMINDER_PROMPT) { launchSingleTop = true }
                }
            })
        }
        composable(Routes.FIRST_REMINDER_PROMPT) {
            BackHandler { navController.popBackStack() }
            FirstReminderPromptScreen(
                onCreateReminder = { navController.navigate(Routes.CREATE_FIRST_REMINDER) { launchSingleTop = true } },
                onSkip = {
                    scope.launch {
                        val granted = preferencesRepository.notificationsEnabled.first()
                        preferencesRepository.completeOnboardingAfterSetup(granted)
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.HELLO) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.CREATE_FIRST_REMINDER) {
            val homeVm: HomeViewModel = viewModel()
            BackHandler { navController.popBackStack() }
            CreateFirstReminderScreen(
                onSave = { title, notes ->
                    homeVm.reminderRepository.createReminder(title, notes)
                    val granted = preferencesRepository.notificationsEnabled.first()
                    preferencesRepository.completeOnboardingAfterSetup(granted)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.HELLO) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.MAIN) { MainTabs(rootNavController = navController) }
        composable(Routes.CREATE_TASK) {
            val homeVm: HomeViewModel = viewModel()
            CreateTaskScreen(
                onSave = { title, notes -> homeVm.taskRepository.createTask(title, notes); navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CREATE_REMINDER) {
            val homeVm: HomeViewModel = viewModel()
            CreateReminderScreen(
                onSave = { title, notes, at -> homeVm.reminderRepository.createReminder(title, notes, at); navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CREATE_NOTE) {
            val homeVm: HomeViewModel = viewModel()
            CreateNoteScreen(
                onSave = { title, body -> homeVm.noteRepository.createNote(title, body); navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CREATE_IDEA) {
            val homeVm: HomeViewModel = viewModel()
            CreateIdeaScreen(
                onSave = { title, body -> homeVm.ideaRepository.createIdea(title, body); navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CREATE_VOICE) {
            val homeVm: HomeViewModel = viewModel()
            VoiceNoteScreen(
                onSave = { title, path, duration -> homeVm.voiceNoteRepository.createVoiceNote(title, path, duration); navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainTabs(rootNavController: NavHostController) {
    val tabNav = rememberNavController()
    var showCapture by remember { mutableStateOf(false) }
    val tabs = listOf(
        Tab(Routes.HOME, "Home", Icons.Outlined.Home),
        Tab(Routes.PLANS, "Plans", Icons.Outlined.TaskAlt),
        Tab(Routes.NOTES, "Notes", Icons.Outlined.Notes),
        Tab(Routes.IDEAS, "Ideas", Icons.Outlined.Lightbulb)
    )
    val backStack by tabNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                    val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNav.navigate(tab.route) {
                                popUpTo(tabNav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { inner ->
        NavHost(navController = tabNav, startDestination = Routes.HOME, modifier = Modifier.padding(inner)) {
            composable(Routes.HOME) { HomeScreen(onQuickCapture = { showCapture = true }) }
            composable(Routes.PLANS) { PlansScreen() }
            composable(Routes.NOTES) { NotesScreen() }
            composable(Routes.IDEAS) { IdeasScreen() }
        }
    }
    if (showCapture) {
        QuickCaptureSheet(
            onSelect = { type ->
                showCapture = false
                val route = when (type) {
                    CaptureType.TASK -> Routes.CREATE_TASK
                    CaptureType.REMINDER -> Routes.CREATE_REMINDER
                    CaptureType.NOTE -> Routes.CREATE_NOTE
                    CaptureType.IDEA -> Routes.CREATE_IDEA
                    CaptureType.VOICE -> Routes.CREATE_VOICE
                }
                rootNavController.navigate(route)
            },
            onDismiss = { showCapture = false }
        )
    }
}

@Composable
fun MyPlannerApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferencesRepository = remember { PreferencesRepository(context.applicationContext) }
    MyPlannerNavGraph(modifier = modifier, preferencesRepository = preferencesRepository)
}
