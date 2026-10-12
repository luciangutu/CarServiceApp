package com.github.luciangutu.carserviceapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val make: String,
    val model: String,
    val year: Int,
    val licensePlate: String,
    val vin: String? = null,
    val notes: String? = null,
    val odometer: Int = 0
)
