package com.lucian.carserviceapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_suggestions")
data class OperationSuggestion(
    @PrimaryKey val name: String
)
