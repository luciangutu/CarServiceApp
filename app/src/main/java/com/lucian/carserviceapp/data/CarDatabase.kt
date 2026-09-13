package com.lucian.carserviceapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lucian.carserviceapp.data.dao.CarDao
import com.lucian.carserviceapp.data.dao.MaintenanceIntervalDao
import com.lucian.carserviceapp.data.dao.OperationSuggestionDao
import com.lucian.carserviceapp.data.dao.ServiceEntryDao
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.entity.MaintenanceInterval
import com.lucian.carserviceapp.data.entity.OperationSuggestion
import com.lucian.carserviceapp.data.entity.ServiceEntry

@Database(
    entities = [
        Car::class,
        ServiceEntry::class,
        MaintenanceInterval::class,
        OperationSuggestion::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CarDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun serviceEntryDao(): ServiceEntryDao
    abstract fun maintenanceIntervalDao(): MaintenanceIntervalDao
    abstract fun operationSuggestionDao(): OperationSuggestionDao
}
