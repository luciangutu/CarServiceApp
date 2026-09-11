package com.lucian.carserviceapp.data.dao

import androidx.room.*
import com.lucian.carserviceapp.data.entity.ServiceEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ServiceEntry): Long

    @Update
    suspend fun update(entry: ServiceEntry)

    @Delete
    suspend fun delete(entry: ServiceEntry)

    @Query("SELECT * FROM service_entries WHERE carId = :carId ORDER BY date DESC")
    fun getEntriesForCar(carId: Long): Flow<List<ServiceEntry>>

    @Query("SELECT * FROM service_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<ServiceEntry>>

    @Query("SELECT DISTINCT operations FROM service_entries")
    suspend fun getUniqueOperations(): List<String>
}
