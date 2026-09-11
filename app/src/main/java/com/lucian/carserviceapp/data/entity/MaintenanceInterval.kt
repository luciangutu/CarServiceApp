package com.lucian.carserviceapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_intervals",
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
data class MaintenanceInterval(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carId: Long,
    val component: String,
    val intervalKm: Int,
    val intervalYears: Int
)
