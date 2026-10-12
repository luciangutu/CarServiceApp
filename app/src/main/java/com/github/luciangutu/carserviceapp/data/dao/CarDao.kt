package com.github.luciangutu.carserviceapp.data.dao

import androidx.room.*
import com.github.luciangutu.carserviceapp.data.entity.Car
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(car: Car): Long

    @Update
    suspend fun update(car: Car)

    @Delete
    suspend fun delete(car: Car)

    @Query("SELECT * FROM cars WHERE id = :id")
    suspend fun getCarById(id: Long): Car?

    @Query("SELECT * FROM cars")
    suspend fun getAllCarsList(): List<Car>

    @Query("SELECT * FROM cars")
    fun getAllCars(): Flow<List<Car>>
}
