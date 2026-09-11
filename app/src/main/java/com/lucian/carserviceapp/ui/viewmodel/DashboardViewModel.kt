package com.lucian.carserviceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CarDashboardItem(
    val car: Car,
    val hasOverdue: Boolean
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: CarRepository
) : ViewModel() {

    val carItems: StateFlow<List<CarDashboardItem>> = combine(
        repository.getAllCars(),
        repository.getAllIntervals(),
        repository.getAllEntries()
    ) { cars, intervals, entries ->
        val currentTime = System.currentTimeMillis()
        cars.map { car ->
            val carIntervals = intervals.filter { it.carId == car.id }
            val carEntries = entries.filter { it.carId == car.id }
            val currentKm = carEntries.maxOfOrNull { it.kilometers } ?: 0

            val hasOverdue = carIntervals.any { interval ->
                val latestEntry = carEntries
                    .filter { it.operations.contains(interval.component, ignoreCase = true) }
                    .maxByOrNull { it.date }

                val dueKm = (latestEntry?.kilometers ?: 0) + interval.intervalKm
                val dueDate = (latestEntry?.date ?: 0L) + interval.intervalYears.toLong() * 365 * 24 * 60 * 60 * 1000

                currentKm >= dueKm || (dueDate > 0 && currentTime >= dueDate)
            }
            CarDashboardItem(car, hasOverdue)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Keep old 'cars' for compatibility if needed, but 'carItems' is better
    val cars: StateFlow<List<Car>> = repository.getAllCars()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
