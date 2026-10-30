package com.lucafilippi.rotaeuropa.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class MainDestination(val route: String, val label: String, val glyph: String)

private val destinations = listOf(
    MainDestination("overview", "Início", "⌂"),
    MainDestination("plan", "Plano", "✓"),
    MainDestination("finance", "Finanças", "€"),
    MainDestination("more", "Mais", "⋯"),
    MainDestination("settings", "Ajustes", "⚙"),
)

@Composable
fun RotaEuropaApp(state: AppUiState, viewModel: RotaEuropaViewModel) {
    val navController = rememberNavController()
    val backStack = navController.currentBackStackEntryAsState()
    val currentRoute = backStack.value?.destination?.route
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(destination.glyph) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "overview",
            modifier = Modifier.padding(padding),
        ) {
            composable("overview") {
                OverviewScreen(
                    state = state,
                    onCompleteTask = viewModel::toggleTask,
                    onEditTargetDate = viewModel::saveTargetDate,
                    onOpenPlan = { navController.navigate("plan") },
                )
            }
            composable("plan") {
                PlanScreen(
                    state = state,
                    onSaveTask = viewModel::saveTask,
                    onDeleteTask = viewModel::deleteTask,
                    onCompleteTask = viewModel::toggleTask,
                )
            }
            composable("finance") {
                FinanceScreen(
                    state = state,
                    onSave = viewModel::saveReserve,
                    onSaveTransaction = viewModel::saveTransaction,
                    onDeleteTransaction = viewModel::deleteTransaction,
                    onSaveBudget = viewModel::saveBudget,
                    onDeleteBudget = viewModel::deleteBudget,
                    onSaveRate = viewModel::saveExchangeRate,
                )
            }
            composable("more") {
                MoreScreen(
                    state = state,
                    onSaveRecord = viewModel::saveJourneyRecord,
                    onDeleteRecord = viewModel::deleteJourneyRecord,
                    onSaveReview = viewModel::saveQuarterlyReview,
                    onOpenBackup = { navController.navigate("backup") },
                )
            }
            composable("backup") {
                BackupScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(state = state, onThemeChange = viewModel::setDarkTheme)
            }
        }
    }
}

