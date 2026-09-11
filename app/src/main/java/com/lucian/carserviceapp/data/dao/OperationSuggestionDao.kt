package com.lucian.carserviceapp.data.dao

import androidx.room.*
import com.lucian.carserviceapp.data.entity.OperationSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationSuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(suggestion: OperationSuggestion)

    @Delete
    suspend fun delete(suggestion: OperationSuggestion)

    @Query("SELECT * FROM operation_suggestions")
    fun getSuggestions(): Flow<List<OperationSuggestion>>
}
