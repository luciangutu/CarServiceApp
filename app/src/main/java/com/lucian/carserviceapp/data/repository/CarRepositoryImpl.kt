package com.lucian.carserviceapp.data.repository

import com.lucian.carserviceapp.data.dao.CarDao
import com.lucian.carserviceapp.data.dao.MaintenanceIntervalDao
import com.lucian.carserviceapp.data.dao.OperationSuggestionDao
import com.lucian.carserviceapp.data.dao.ServiceEntryDao
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.entity.MaintenanceInterval
import com.lucian.carserviceapp.data.entity.OperationSuggestion
import com.lucian.carserviceapp.data.entity.ServiceEntry
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
    override fun getAllCars(): Flow<List<Car>> = carDao.getAllCars()

    override suspend fun insertMaintenanceInterval(interval: MaintenanceInterval): Long = maintenanceIntervalDao.insert(interval)
    override suspend fun updateMaintenanceInterval(interval: MaintenanceInterval) = maintenanceIntervalDao.update(interval)
    override suspend fun deleteMaintenanceInterval(interval: MaintenanceInterval) = maintenanceIntervalDao.delete(interval)
    override fun getIntervalsForCar(carId: Long): Flow<List<MaintenanceInterval>> = maintenanceIntervalDao.getIntervalsForCar(carId)
    override fun getAllIntervals(): Flow<List<MaintenanceInterval>> = maintenanceIntervalDao.getAllIntervals()

    override suspend fun insertOperationSuggestion(suggestion: OperationSuggestion) = operationSuggestionDao.insert(suggestion)
    override suspend fun deleteOperationSuggestion(suggestion: OperationSuggestion) = operationSuggestionDao.delete(suggestion)
    override fun getSuggestions(): Flow<List<OperationSuggestion>> = operationSuggestionDao.getSuggestions()

    override suspend fun insertServiceEntry(entry: ServiceEntry): Long = serviceEntryDao.insert(entry)
    override suspend fun updateServiceEntry(entry: ServiceEntry) = serviceEntryDao.update(entry)
    override suspend fun deleteServiceEntry(entry: ServiceEntry) = serviceEntryDao.delete(entry)
    override suspend fun getServiceEntryById(id: Long): ServiceEntry? = serviceEntryDao.getServiceById(id)
    override fun getEntriesForCar(carId: Long): Flow<List<ServiceEntry>> = serviceEntryDao.getEntriesForCar(carId)
    override fun getAllEntries(): Flow<List<ServiceEntry>> = serviceEntryDao.getAllEntries()
    override suspend fun getUniqueOperations(): List<String> = serviceEntryDao.getUniqueOperations()
}
