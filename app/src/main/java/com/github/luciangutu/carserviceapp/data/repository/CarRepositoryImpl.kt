package com.github.luciangutu.carserviceapp.data.repository

import com.github.luciangutu.carserviceapp.data.dao.CarDao
import com.github.luciangutu.carserviceapp.data.dao.MaintenanceIntervalDao
import com.github.luciangutu.carserviceapp.data.dao.OperationSuggestionDao
import com.github.luciangutu.carserviceapp.data.dao.ServiceEntryDao
import com.github.luciangutu.carserviceapp.data.entity.Car
import com.github.luciangutu.carserviceapp.data.entity.MaintenanceInterval
import com.github.luciangutu.carserviceapp.data.entity.OperationSuggestion
import com.github.luciangutu.carserviceapp.data.entity.ServiceEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepositoryImpl @Inject constructor(
    private val carDao: CarDao,
    private val maintenanceIntervalDao: MaintenanceIntervalDao,
    private val operationSuggestionDao: OperationSuggestionDao,
    private val serviceEntryDao: ServiceEntryDao
) : CarRepository {

    override suspend fun insertCar(car: Car): Long = carDao.insert(car)
    override suspend fun updateCar(car: Car) = carDao.update(car)
    override suspend fun deleteCar(car: Car) = carDao.delete(car)
    override suspend fun getCarById(id: Long): Car? = carDao.getCarById(id)
    override suspend fun getAllCarsList(): List<Car> = carDao.getAllCarsList()
    override fun getAllCars(): Flow<List<Car>> = carDao.getAllCars()

    override suspend fun insertMaintenanceInterval(interval: MaintenanceInterval): Long = maintenanceIntervalDao.insert(interval)
    override suspend fun updateMaintenanceInterval(interval: MaintenanceInterval) = maintenanceIntervalDao.update(interval)
    override suspend fun deleteMaintenanceInterval(interval: MaintenanceInterval) = maintenanceIntervalDao.delete(interval)
    override fun getIntervalsForCar(carId: Long): Flow<List<MaintenanceInterval>> = maintenanceIntervalDao.getIntervalsForCar(carId)
    override suspend fun getAllIntervalsList(): List<MaintenanceInterval> = maintenanceIntervalDao.getAllIntervalsList()
    override fun getAllIntervals(): Flow<List<MaintenanceInterval>> = maintenanceIntervalDao.getAllIntervals()

    override suspend fun insertOperationSuggestion(suggestion: OperationSuggestion) = operationSuggestionDao.insert(suggestion)
    override suspend fun deleteOperationSuggestion(suggestion: OperationSuggestion) = operationSuggestionDao.delete(suggestion)
    override suspend fun getSuggestionsList(): List<OperationSuggestion> = operationSuggestionDao.getSuggestionsList()
    override fun getSuggestions(): Flow<List<OperationSuggestion>> = operationSuggestionDao.getSuggestions()

    override suspend fun insertServiceEntry(entry: ServiceEntry): Long = serviceEntryDao.insert(entry)
    override suspend fun updateServiceEntry(entry: ServiceEntry) = serviceEntryDao.update(entry)
    override suspend fun deleteServiceEntry(entry: ServiceEntry) = serviceEntryDao.delete(entry)
    override suspend fun getServiceEntryById(id: Long): ServiceEntry? = serviceEntryDao.getServiceById(id)
    override fun getEntriesForCar(carId: Long): Flow<List<ServiceEntry>> = serviceEntryDao.getEntriesForCar(carId)
    override suspend fun getAllEntriesList(): List<ServiceEntry> = serviceEntryDao.getAllEntriesList()
    override fun getAllEntries(): Flow<List<ServiceEntry>> = serviceEntryDao.getAllEntries()
    override suspend fun getUniqueOperations(): List<String> = serviceEntryDao.getUniqueOperations()
}
