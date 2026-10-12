package com.github.luciangutu.carserviceapp.di

import android.content.Context
import androidx.room.Room
import com.github.luciangutu.carserviceapp.data.CarDatabase
import com.github.luciangutu.carserviceapp.data.dao.CarDao
import com.github.luciangutu.carserviceapp.data.dao.MaintenanceIntervalDao
import com.github.luciangutu.carserviceapp.data.dao.OperationSuggestionDao
import com.github.luciangutu.carserviceapp.data.dao.ServiceEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE cars ADD COLUMN odometer INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideCarDatabase(
        @ApplicationContext context: Context
    ): CarDatabase {
        return Room.databaseBuilder(
            context,
            CarDatabase::class.java,
            "car_service_database"
        ).addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
            .build()
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
