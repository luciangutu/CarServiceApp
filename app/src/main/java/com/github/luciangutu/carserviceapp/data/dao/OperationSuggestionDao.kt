package com.github.luciangutu.carserviceapp.data.dao

import androidx.room.*
import com.github.luciangutu.carserviceapp.data.entity.OperationSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationSuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(suggestion: OperationSuggestion)

    @Delete
    suspend fun delete(suggestion: OperationSuggestion)

    @Query("SELECT * FROM operation_suggestions")
    suspend fun getSuggestionsList(): List<OperationSuggestion>

    @Query("SELECT * FROM operation_suggestions")
    fun getSuggestions(): Flow<List<OperationSuggestion>>
}
