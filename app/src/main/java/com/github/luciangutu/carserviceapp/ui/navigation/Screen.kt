package com.github.luciangutu.carserviceapp.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CarDetail : Screen("car_detail/{carId}") {
        fun createRoute(carId: Long) = "car_detail/$carId"
    }
    object CarAddEdit : Screen("car_add_edit/{carId}") {
        fun createRoute(carId: Long = -1L) = "car_add_edit/$carId"
    }
    object ServiceAddEdit : Screen("service_add_edit/{carId}?serviceId={serviceId}") {
        fun createRoute(carId: Long, serviceId: Long? = null) = 
            if (serviceId != null) "service_add_edit/$carId?serviceId=$serviceId" else "service_add_edit/$carId"
    }
    object MaintenanceIntervals : Screen("maintenance_intervals/{carId}") {
        fun createRoute(carId: Long) = "maintenance_intervals/$carId"
    }
    object Settings : Screen("settings")
}
