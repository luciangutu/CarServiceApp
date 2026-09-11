package com.lucian.carserviceapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lucian.carserviceapp.ui.screens.DashboardScreen
import com.lucian.carserviceapp.ui.screens.CarAddEditScreen
import com.lucian.carserviceapp.ui.screens.CarDetailScreen
import com.lucian.carserviceapp.ui.screens.ServiceAddEditScreen
import com.lucian.carserviceapp.ui.screens.MaintenanceIntervalsScreen
import com.lucian.carserviceapp.ui.screens.SettingsScreen
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onAddCarClick = {
                    navController.navigate(Screen.CarAddEdit.createRoute())
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(navArgument("carId") { type = NavType.LongType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getLong("carId") ?: -1L
            CarDetailScreen(
                carId = carId,
                onAddServiceClick = { id ->
                    navController.navigate(Screen.ServiceAddEdit.createRoute(id))
                },
                onEditServiceClick = { id, serviceId ->
                    navController.navigate(Screen.ServiceAddEdit.createRoute(id, serviceId))
                },
                onMaintenanceClick = { id ->
                    navController.navigate(Screen.MaintenanceIntervals.createRoute(id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MaintenanceIntervals.route,
            arguments = listOf(navArgument("carId") { type = NavType.LongType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getLong("carId") ?: -1L
            MaintenanceIntervalsScreen(
                carId = carId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CarAddEdit.route,
            arguments = listOf(navArgument("carId") { 
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getLong("carId") ?: -1L
            CarAddEditScreen(
                carId = carId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ServiceAddEdit.route,
            arguments = listOf(
                navArgument("carId") { type = NavType.LongType },
                navArgument("serviceId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getLong("carId") ?: -1L
            val serviceIdArg = backStackEntry.arguments?.getLong("serviceId") ?: -1L
            val serviceId = if (serviceIdArg == -1L) 0L else serviceIdArg
            
            ServiceAddEditScreen(
                carId = carId,
                serviceId = serviceId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$name Screen Placeholder")
    }
}
