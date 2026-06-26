package com.sleeplife.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sleeplife.app.ui.screens.habits.HabitsScreen
import com.sleeplife.app.ui.screens.more.MoreScreen
import com.sleeplife.app.ui.screens.notes.NotesScreen
import com.sleeplife.app.ui.screens.pomodoro.PomodoroScreen
import com.sleeplife.app.ui.screens.sleep.SleepScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Sleep : Screen("sleep", "睡眠", Icons.Default.Brightness2)
    object Habits : Screen("habits", "习惯", Icons.Default.TaskAlt)
    object Pomodoro : Screen("pomodoro", "专注", Icons.Default.AccessTime)
    object Notes : Screen("notes", "笔记", Icons.Default.NoteAdd)
    object More : Screen("more", "更多", Icons.Default.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepLifeNavGraph() {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.Sleep,
        Screen.Habits,
        Screen.Pomodoro,
        Screen.Notes,
        Screen.More
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Sleep.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Sleep.route) { SleepScreen() }
            composable(Screen.Habits.route) { HabitsScreen() }
            composable(Screen.Pomodoro.route) { PomodoroScreen() }
            composable(Screen.Notes.route) { NotesScreen() }
            composable(Screen.More.route) { MoreScreen() }
        }
    }
}
