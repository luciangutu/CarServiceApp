package com.lucian.carserviceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.hasOperation
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val carItems: StateFlow<List<CarDashboardItem>> = combine(
        repository.getAllCars(),
        repository.getAllIntervals(),
        repository.getAllEntries(),
        _searchQuery
    ) { cars, intervals, entries, query ->
        val currentTime = System.currentTimeMillis()
        val filteredCars = if (query.isBlank()) {
            cars
        } else {
            val queryParts = query.trim().split("\\s+".toRegex())
            cars.filter { car ->
                queryParts.all { part ->
                    car.make.contains(part, ignoreCase = true) ||
                            car.model.contains(part, ignoreCase = true) ||
                            car.licensePlate.contains(part, ignoreCase = true)
                }
            }
        }

        filteredCars.map { car ->
            val carIntervals = intervals.filter { it.carId == car.id }
            val carEntries = entries.filter { it.carId == car.id }
            val currentKm = carEntries.maxOfOrNull { it.kilometers } ?: 0

            val hasOverdue = carIntervals.any { interval ->
                val latestEntry = carEntries
                    .filter { it.hasOperation(interval.component) }
                    .maxByOrNull { it.date }

                val isKmOverdue = if (interval.intervalKm > 0) {
                    val dueKm = (latestEntry?.kilometers ?: 0) + interval.intervalKm
                    currentKm >= dueKm
                } else {
                    false
                }

                val isDateOverdue = if (interval.intervalYears > 0) {
                    val dueDate = (latestEntry?.date ?: 0L) +
                            interval.intervalYears.toLong() * 365 * 24 * 60 * 60 * 1000
                    dueDate > 0 && currentTime >= dueDate
                } else {
                    false
                }

                isKmOverdue || isDateOverdue
            }
            CarDashboardItem(car, hasOverdue)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Keep old 'cars' for compatibility if needed, but 'carItems' is better
    val cars: StateFlow<List<Car>> = repository.getAllCars()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
