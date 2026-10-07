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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myplanner.app.data.local.PreferencesRepository
import com.myplanner.app.ui.calendar.CalendarScreen
import com.myplanner.app.ui.capture.CaptureType
import com.myplanner.app.ui.capture.QuickCaptureSheet
import com.myplanner.app.ui.hello.HelloScreen
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.PlanKind
import com.myplanner.app.ui.idea.IdeaEditorScreen
import com.myplanner.app.ui.idea.IdeasScreen
import com.myplanner.app.ui.note.NoteEditorScreen
import com.myplanner.app.ui.note.NotesScreen
import com.myplanner.app.ui.pete.AmbientHomeScreen
import com.myplanner.app.ui.pete.ConversationScreen
import com.myplanner.app.ui.plans.PlansScreen
import com.myplanner.app.ui.reminder.CreateFirstReminderScreen
import com.myplanner.app.ui.reminder.CreateReminderScreen
import com.myplanner.app.ui.reminder.ReminderEditorScreen
import com.myplanner.app.ui.search.SearchScreen
import com.myplanner.app.ui.settings.PrivacyScreen
import com.myplanner.app.ui.settings.SettingsScreen
import com.myplanner.app.ui.setup.FirstReminderPromptScreen
import com.myplanner.app.ui.setup.NotificationSetupScreen
import com.myplanner.app.ui.task.CreateTaskScreen
import com.myplanner.app.ui.task.TaskEditorScreen
import com.myplanner.app.ui.upcoming.UpcomingScreen
import com.myplanner.app.ui.voice.VoiceNotePlayerScreen
import com.myplanner.app.ui.voice.VoiceNoteScreen
import com.myplanner.app.ui.voice.VoiceNotesListScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object Routes {
    const val HELLO = "hello"
    const val NOTIFICATION_SETUP = "notification_setup"
    const val FIRST_REMINDER_PROMPT = "first_reminder_prompt"
    const val CREATE_FIRST_REMINDER = "create_first_reminder"
    const val MAIN = "main"
    const val HOME = "home"
    const val CONVERSATION = "pete_conversation"
    const val PLANS = "plans"
    const val NOTES = "notes"
    const val IDEAS = "ideas"
    const val CREATE_TASK = "create_task"
    const val CREATE_REMINDER = "create_reminder"
    const val CREATE_NOTE = "create_note"
    const val CREATE_IDEA = "create_idea"
    const val CREATE_VOICE = "create_voice"
    const val VOICE_NOTES = "voice_notes"
    const val CALENDAR = "calendar"
    const val UPCOMING = "upcoming"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val VOICE_DETAIL = "voice_detail/{noteId}"
    const val EDIT_TASK = "edit_task/{taskId}"
    const val EDIT_REMINDER = "edit_reminder/{reminderId}"
    const val EDIT_NOTE = "edit_note/{noteId}"
    const val EDIT_IDEA = "edit_idea/{ideaId}"
    fun editTask(id: Long) = "edit_task/$id"
    fun editReminder(id: Long) = "edit_reminder/$id"
    fun editNote(id: Long) = "edit_note/$id"
    fun editIdea(id: Long) = "edit_idea/$id"
    fun voiceDetail(id: Long) = "voice_detail/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MyPlannerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    preferencesRepository: PreferencesRepository,
    openReminderId: Long? = null
) {
    val scope = rememberCoroutineScope()
    var startReady by remember { mutableStateOf(false) }
    var onboardingComplete by remember { mutableStateOf(false) }
    LaunchedEffect(preferencesRepository) {
        onboardingComplete = preferencesRepository.onboardingComplete.first()
        startReady = true
    }
    LaunchedEffect(openReminderId, startReady, onboardingComplete) {
        if (startReady && onboardingComplete && openReminderId != null && openReminderId > 0) {
            navController.navigate(Routes.editReminder(openReminderId)) { launchSingleTop = true }
        }
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
                            popUpTo(Routes.HELLO) { inclusive = true }; launchSingleTop = true
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
                            popUpTo(Routes.HELLO) { inclusive = true }; launchSingleTop = true
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
                        popUpTo(Routes.HELLO) { inclusive = true }; launchSingleTop = true
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CONVERSATION) {
            ConversationScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MAIN) { MainTabs(rootNavController = navController) }
        composable(Routes.CREATE_TASK) {
            val homeVm: HomeViewModel = viewModel()
            CreateTaskScreen(
                onSave = { title, notes, dueAt, priority ->
                    homeVm.taskRepository.createTask(title, notes, dueAt, priority)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CREATE_REMINDER) {
            val homeVm: HomeViewModel = viewModel()
            CreateReminderScreen(
                onSave = { title, notes, at, repeat ->
                    homeVm.reminderRepository.createReminder(title, notes, at, repeat)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.CREATE_NOTE) {
            NoteEditorScreen(noteId = null, onDone = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }
        composable(Routes.CREATE_IDEA) {
            IdeaEditorScreen(ideaId = null, onDone = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }
        composable(Routes.CREATE_VOICE) {
            val homeVm: HomeViewModel = viewModel()
            VoiceNoteScreen(
                onSave = { title, path, duration ->
                    homeVm.voiceNoteRepository.createVoiceNote(title, path, duration)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.VOICE_NOTES) {
            VoiceNotesListScreen(
                onOpen = { navController.navigate(Routes.voiceDetail(it)) },
                onRecord = { navController.navigate(Routes.CREATE_VOICE) }
            )
        }
        composable(Routes.VOICE_DETAIL, arguments = listOf(navArgument("noteId") { type = NavType.LongType })) { entry ->
            val noteId = entry.arguments?.getLong("noteId") ?: return@composable
            VoiceNotePlayerScreen(noteId = noteId, onDone = { navController.popBackStack() })
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(
                onOpenItem = { item ->
                    when (item.kind) {
                        PlanKind.TASK -> navController.navigate(Routes.editTask(item.id))
                        PlanKind.REMINDER -> navController.navigate(Routes.editReminder(item.id))
                    }
                },
                onCreate = { navController.navigate(Routes.CREATE_TASK) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.UPCOMING) {
            UpcomingScreen(
                onOpenItem = { item ->
                    when (item.kind) {
                        PlanKind.TASK -> navController.navigate(Routes.editTask(item.id))
                        PlanKind.REMINDER -> navController.navigate(Routes.editReminder(item.id))
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onOpenTask = { navController.navigate(Routes.editTask(it)) },
                onOpenReminder = { navController.navigate(Routes.editReminder(it)) },
                onOpenNote = { navController.navigate(Routes.editNote(it)) },
                onOpenIdea = { navController.navigate(Routes.editIdea(it)) },
                onOpenVoice = { navController.navigate(Routes.voiceDetail(it)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EDIT_TASK, arguments = listOf(navArgument("taskId") { type = NavType.LongType })) { entry ->
            val taskId = entry.arguments?.getLong("taskId") ?: return@composable
            TaskEditorScreen(taskId = taskId, onDone = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }
        composable(Routes.EDIT_REMINDER, arguments = listOf(navArgument("reminderId") { type = NavType.LongType })) { entry ->
            val reminderId = entry.arguments?.getLong("reminderId") ?: return@composable
            ReminderEditorScreen(reminderId = reminderId, onDone = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }
        composable(Routes.EDIT_NOTE, arguments = listOf(navArgument("noteId") { type = NavType.LongType })) { entry ->
            val noteId = entry.arguments?.getLong("noteId") ?: return@composable
            NoteEditorScreen(noteId = noteId, onDone = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }
        composable(Routes.EDIT_IDEA, arguments = listOf(navArgument("ideaId") { type = NavType.LongType })) { entry ->
            val ideaId = entry.arguments?.getLong("ideaId") ?: return@composable
            IdeaEditorScreen(ideaId = ideaId, onDone = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
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
    val onAmbientHome = currentRoute?.hierarchy?.any { it.route == Routes.HOME } == true
    Scaffold(
        containerColor = if (onAmbientHome) androidx.compose.ui.graphics.Color.Transparent
        else MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!onAmbientHome) {
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
        }
    ) { inner ->
        NavHost(
            navController = tabNav,
            startDestination = Routes.HOME,
            modifier = if (onAmbientHome) Modifier.fillMaxSize() else Modifier.padding(inner)
        ) {
            composable(Routes.HOME) {
                AmbientHomeScreen(
                    onHeyPete = { rootNavController.navigate(Routes.CONVERSATION) },
                    onOpenItem = { item ->
                        when (item.kind) {
                            PlanKind.TASK -> rootNavController.navigate(Routes.editTask(item.id))
                            PlanKind.REMINDER -> rootNavController.navigate(Routes.editReminder(item.id))
                        }
                    }
                )
            }
            composable(Routes.PLANS) {
                PlansScreen(
                    onOpenTask = { rootNavController.navigate(Routes.editTask(it)) },
                    onOpenReminder = { rootNavController.navigate(Routes.editReminder(it)) }
                )
            }
            composable(Routes.NOTES) {
                NotesScreen(
                    onOpenNote = { rootNavController.navigate(Routes.editNote(it)) },
                    onCreateNote = { rootNavController.navigate(Routes.CREATE_NOTE) },
                    onOpenVoiceNotes = { rootNavController.navigate(Routes.VOICE_NOTES) }
                )
            }
            composable(Routes.IDEAS) {
                IdeasScreen(
                    onOpenIdea = { rootNavController.navigate(Routes.editIdea(it)) },
                    onCreateIdea = { rootNavController.navigate(Routes.CREATE_IDEA) }
                )
            }
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
fun MyPlannerApp(modifier: Modifier = Modifier, openReminderId: Long? = null) {
    val context = LocalContext.current
    val preferencesRepository = remember { PreferencesRepository(context.applicationContext) }
    MyPlannerNavGraph(modifier = modifier, preferencesRepository = preferencesRepository, openReminderId = openReminderId)
}
