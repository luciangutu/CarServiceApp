package com.lucian.carserviceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarViewModel @Inject constructor(
    private val repository: CarRepository
) : ViewModel() {

    private val _car = MutableStateFlow<Car?>(null)
    val car: StateFlow<Car?> = _car.asStateFlow()

    fun getCarById(id: Long) {
        if (id == -1L) {
            _car.value = null
            return
        }
        viewModelScope.launch {
            _car.value = repository.getCarById(id)
        }
    }

    fun saveCar(
        id: Long = 0,
        make: String,
        model: String,
        year: Int,
        licensePlate: String,
        vin: String?,
        notes: String?,
        odometer: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val car = Car(
                id = if (id == -1L) 0 else id,
                make = make,
                model = model,
                year = year,
                licensePlate = licensePlate,
                vin = vin,
                notes = notes,
                odometer = odometer
            )
            if (car.id == 0L) {
                repository.insertCar(car)
            } else {
                repository.updateCar(car)
            }
            onSuccess()
        }
    }

    fun deleteCar(onSuccess: () -> Unit) {
        val currentCar = _car.value ?: return
        viewModelScope.launch {
            repository.deleteCar(currentCar)
            onSuccess()
        }
    }
}
