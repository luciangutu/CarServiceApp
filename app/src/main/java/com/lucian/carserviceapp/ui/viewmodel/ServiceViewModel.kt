package com.lucian.carserviceapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.formatServiceOperations
import com.lucian.carserviceapp.data.matchesServiceSearch
import com.lucian.carserviceapp.data.parseServiceEntryImport
import com.lucian.carserviceapp.data.parseServiceOperations
import com.lucian.carserviceapp.data.entity.OperationSuggestion
import com.lucian.carserviceapp.data.entity.ServiceEntry
import com.lucian.carserviceapp.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: CarRepository
) : ViewModel() {

    private val _serviceEntries = MutableStateFlow<List<ServiceEntry>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _serviceEntry = MutableStateFlow<ServiceEntry?>(null)
    val serviceEntry: StateFlow<ServiceEntry?> = _serviceEntry.asStateFlow()

    val filteredServiceEntries: StateFlow<List<ServiceEntry>> = combine(
        _serviceEntries,
        _searchQuery
    ) { entries, query ->
        if (query.isBlank()) {
            entries
        } else {
            entries.filter { it.matchesServiceSearch(query) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<OperationSuggestion>> = repository.getSuggestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchEntriesForCar(carId: Long) {
        viewModelScope.launch {
            repository.getEntriesForCar(carId).collect {
                _serviceEntries.value = it
            }
        }
    }

    fun getServiceEntryById(id: Long) {
        if (id == 0L) {
            _serviceEntry.value = null
            return
        }
        viewModelScope.launch {
            _serviceEntry.value = repository.getServiceEntryById(id)
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
            val operationsString = formatServiceOperations(operations)
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

    fun deleteServiceEntry(entry: ServiceEntry) {
        viewModelScope.launch {
            repository.deleteServiceEntry(entry)
        }
    }

    fun importServiceEntries(carId: Long, uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val content = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()

                    val importResult = parseServiceEntryImport(carId, content)
                    importResult.entries.forEach { entry ->
                        repository.insertServiceEntry(entry)
                        parseServiceOperations(entry.operations).forEach { operation ->
                            repository.insertOperationSuggestion(OperationSuggestion(operation))
                        }
                    }
                    importResult
                }

                val skippedText = if (result.skippedRows > 0) {
                    " ${result.skippedRows} rows skipped."
                } else {
                    ""
                }
                _message.emit("Imported ${result.entries.size} service entries.$skippedText")
            } catch (e: Exception) {
                _message.emit("Import failed: ${e.message}")
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
