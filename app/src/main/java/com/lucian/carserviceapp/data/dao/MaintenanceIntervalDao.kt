package com.lucian.carserviceapp.data.dao

import androidx.room.*
import com.lucian.carserviceapp.data.entity.MaintenanceInterval
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceIntervalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interval: MaintenanceInterval): Long

    @Update
    suspend fun update(interval: MaintenanceInterval)

    @Delete
    suspend fun delete(interval: MaintenanceInterval)

    @Query("SELECT * FROM maintenance_intervals WHERE carId = :carId")
    fun getIntervalsForCar(carId: Long): Flow<List<MaintenanceInterval>>

    @Query("SELECT * FROM maintenance_intervals")
    fun getAllIntervals(): Flow<List<MaintenanceInterval>>
}
