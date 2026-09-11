package com.lucian.carserviceapp.di

import android.content.Context
import androidx.room.Room
import com.lucian.carserviceapp.data.CarDatabase
import com.lucian.carserviceapp.data.dao.CarDao
import com.lucian.carserviceapp.data.dao.MaintenanceIntervalDao
import com.lucian.carserviceapp.data.dao.OperationSuggestionDao
import com.lucian.carserviceapp.data.dao.ServiceEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCarDatabase(
        @ApplicationContext context: Context
    ): CarDatabase {
        return Room.databaseBuilder(
            context,
            CarDatabase::class.java,
            "car_service_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCarDao(database: CarDatabase): CarDao {
        return database.carDao()
    }

    @Provides
    @Singleton
    fun provideMaintenanceIntervalDao(database: CarDatabase): MaintenanceIntervalDao {
        return database.maintenanceIntervalDao()
    }

    @Provides
    @Singleton
    fun provideOperationSuggestionDao(database: CarDatabase): OperationSuggestionDao {
        return database.operationSuggestionDao()
    }

    @Provides
    @Singleton
    fun provideServiceEntryDao(database: CarDatabase): ServiceEntryDao {
        return database.serviceEntryDao()
    }
}
