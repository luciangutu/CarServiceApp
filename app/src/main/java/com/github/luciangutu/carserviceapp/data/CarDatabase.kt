package com.github.luciangutu.carserviceapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.luciangutu.carserviceapp.data.dao.CarDao
import com.github.luciangutu.carserviceapp.data.dao.MaintenanceIntervalDao
import com.github.luciangutu.carserviceapp.data.dao.OperationSuggestionDao
import com.github.luciangutu.carserviceapp.data.dao.ServiceEntryDao
import com.github.luciangutu.carserviceapp.data.entity.Car
import com.github.luciangutu.carserviceapp.data.entity.MaintenanceInterval
import com.github.luciangutu.carserviceapp.data.entity.OperationSuggestion
import com.github.luciangutu.carserviceapp.data.entity.ServiceEntry

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
