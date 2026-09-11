package com.lucian.carserviceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.entity.MaintenanceInterval
import com.lucian.carserviceapp.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceStatus(
    val interval: MaintenanceInterval,
    val component: String,
    val dueKm: Int,
    val dueDate: Long,
    val status: String
)

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: CarRepository
) : ViewModel() {

    fun getMaintenanceStatus(carId: Long): Flow<List<MaintenanceStatus>> {
        return combine(
            repository.getIntervalsForCar(carId),
            repository.getEntriesForCar(carId)
        ) { intervals, entries ->
            val currentKm = entries.maxOfOrNull { it.kilometers } ?: 0
            val currentTime = System.currentTimeMillis()

            intervals.map { interval ->
                val latestEntry = entries
                    .filter { it.operations.contains(interval.component, ignoreCase = true) }
                    .maxByOrNull { it.date }

                val dueKm = (latestEntry?.kilometers ?: 0) + interval.intervalKm
                val dueDate = (latestEntry?.date ?: 0L) + interval.intervalYears.toLong() * 365 * 24 * 60 * 60 * 1000

                val status = when {
                    currentKm >= dueKm || (dueDate > 0 && currentTime >= dueDate) -> "Overdue"
                    currentKm >= dueKm - 1000 || (dueDate > 0 && currentTime >= dueDate - (30L * 24 * 60 * 60 * 1000)) -> "Upcoming"
                    else -> "OK"
                }

                MaintenanceStatus(
                    interval = interval,
                    component = interval.component,
                    dueKm = dueKm,
                    dueDate = dueDate,
                    status = status
                )
            }
        }
    }

    fun addInterval(carId: Long, component: String, intervalKm: Int, intervalYears: Int) {
        viewModelScope.launch {
            repository.insertMaintenanceInterval(
                MaintenanceInterval(
                    carId = carId,
                    component = component,
                    intervalKm = intervalKm,
                    intervalYears = intervalYears
                )
            )
        }
    }

    fun deleteInterval(interval: MaintenanceInterval) {
        viewModelScope.launch {
            repository.deleteMaintenanceInterval(interval)
        }
    }
}
