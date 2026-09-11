package com.lucian.carserviceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.entity.OperationSuggestion
import com.lucian.carserviceapp.data.entity.ServiceEntry
import com.lucian.carserviceapp.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val repository: CarRepository
) : ViewModel() {

    private val _serviceEntries = MutableStateFlow<List<ServiceEntry>>(emptyList())
    val serviceEntries: StateFlow<List<ServiceEntry>> = _serviceEntries.asStateFlow()

    val suggestions: StateFlow<List<OperationSuggestion>> = repository.getSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchEntriesForCar(carId: Long) {
        viewModelScope.launch {
            repository.getEntriesForCar(carId).collect {
                _serviceEntries.value = it
            }
        }
    }

    fun saveServiceEntry(
        id: Long = 0,
        carId: Long,
        date: Long,
        kilometers: Int,
        serviceName: String,
        operations: List<String>,
        cost: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val operationsString = operations.joinToString(",")
            val entry = ServiceEntry(
                id = id,
                carId = carId,
                date = date,
                kilometers = kilometers,
                serviceName = serviceName,
                operations = operationsString,
                cost = cost
            )

            if (id == 0L) {
                repository.insertServiceEntry(entry)
            } else {
                repository.updateServiceEntry(entry)
            }

            // Extract tags and add to suggestions if new
            operations.forEach { op ->
                if (op.isNotBlank()) {
                    repository.insertOperationSuggestion(OperationSuggestion(op.trim()))
                }
            }

            onSuccess()
        }
    }
}
