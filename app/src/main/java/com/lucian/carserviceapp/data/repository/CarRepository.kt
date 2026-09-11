package com.lucian.carserviceapp.data.repository

import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.entity.MaintenanceInterval
import com.lucian.carserviceapp.data.entity.OperationSuggestion
import com.lucian.carserviceapp.data.entity.ServiceEntry
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    // Car operations
    suspend fun insertCar(car: Car): Long
    suspend fun updateCar(car: Car)
    suspend fun deleteCar(car: Car)
    suspend fun getCarById(id: Long): Car?
    fun getAllCars(): Flow<List<Car>>

    // Maintenance Interval operations
    suspend fun insertMaintenanceInterval(interval: MaintenanceInterval): Long
    suspend fun updateMaintenanceInterval(interval: MaintenanceInterval)
    suspend fun deleteMaintenanceInterval(interval: MaintenanceInterval)
    fun getIntervalsForCar(carId: Long): Flow<List<MaintenanceInterval>>
    fun getAllIntervals(): Flow<List<MaintenanceInterval>>

    // Operation Suggestion operations
    suspend fun insertOperationSuggestion(suggestion: OperationSuggestion)
    suspend fun deleteOperationSuggestion(suggestion: OperationSuggestion)
    fun getSuggestions(): Flow<List<OperationSuggestion>>

    // Service Entry operations
    suspend fun insertServiceEntry(entry: ServiceEntry): Long
    suspend fun updateServiceEntry(entry: ServiceEntry)
    suspend fun deleteServiceEntry(entry: ServiceEntry)
    fun getEntriesForCar(carId: Long): Flow<List<ServiceEntry>>
    fun getAllEntries(): Flow<List<ServiceEntry>>
    suspend fun getUniqueOperations(): List<String>
}
