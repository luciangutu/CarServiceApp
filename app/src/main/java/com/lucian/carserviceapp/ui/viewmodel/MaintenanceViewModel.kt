package com.lucian.carserviceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.hasOperation
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
    val dueDate: Long?,
    val lastDoneKm: Int?,
    val lastDoneDate: Long?,
    val status: String
)

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: CarRepository
) : ViewModel() {

    fun getMaintenanceStatus(carId: Long): Flow<List<MaintenanceStatus>> {
        return combine(
            repository.getIntervalsForCar(carId),
            repository.getEntriesForCar(carId),
            flow { emit(repository.getCarById(carId)) }
        ) { intervals, entries, car ->
            val maxEntryKm = entries.maxOfOrNull { it.kilometers } ?: 0
            val carOdometer = car?.odometer ?: 0
            val currentKm = maxOf(maxEntryKm, carOdometer)
            val currentTime = System.currentTimeMillis()

            intervals.map { interval ->
                val latestEntry = entries
                    .filter { it.hasOperation(interval.component) }
                    .maxByOrNull { it.date }

                val dueKm = if (interval.intervalKm > 0) {
                    (latestEntry?.kilometers ?: 0) + interval.intervalKm
                } else {
                    0
                }
                val dueDate = if (interval.intervalYears > 0 && latestEntry != null) {
                    (latestEntry?.date ?: 0L) + interval.intervalYears.toLong() * 365 * 24 * 60 * 60 * 1000
                } else {
                    null
                }
                val isKmOverdue = interval.intervalKm > 0 && currentKm >= dueKm
                val isDateOverdue = dueDate != null && currentTime >= dueDate
                val isKmUpcoming = interval.intervalKm > 0 && currentKm >= dueKm - 1000
                val isDateUpcoming = dueDate != null &&
                        currentTime >= dueDate - (30L * 24 * 60 * 60 * 1000)

                val status = when {
                    isKmOverdue || isDateOverdue -> "Overdue"
                    isKmUpcoming || isDateUpcoming -> "Upcoming"
                    else -> "OK"
                }

                MaintenanceStatus(
                    interval = interval,
                    component = interval.component,
                    dueKm = dueKm,
                    dueDate = dueDate,
                    lastDoneKm = latestEntry?.kilometers,
                    lastDoneDate = latestEntry?.date,
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

    fun updateInterval(interval: MaintenanceInterval, component: String, intervalKm: Int, intervalYears: Int) {
        viewModelScope.launch {
            repository.updateMaintenanceInterval(
                interval.copy(
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
