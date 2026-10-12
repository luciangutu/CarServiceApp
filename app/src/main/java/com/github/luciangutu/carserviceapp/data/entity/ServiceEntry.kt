package com.github.luciangutu.carserviceapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_entries",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carId")]
)
data class ServiceEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carId: Long,
    val date: Long, // Timestamp
    val kilometers: Int,
    val serviceName: String,
    val operations: String, // Comma-separated or similar
    val cost: Double? = null
)
