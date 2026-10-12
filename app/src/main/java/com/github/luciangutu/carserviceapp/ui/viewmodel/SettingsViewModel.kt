package com.github.luciangutu.carserviceapp.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.luciangutu.carserviceapp.data.CarDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.google.gson.GsonBuilder
import com.github.luciangutu.carserviceapp.data.entity.Car
import com.github.luciangutu.carserviceapp.data.entity.MaintenanceInterval
import com.github.luciangutu.carserviceapp.data.entity.OperationSuggestion
import com.github.luciangutu.carserviceapp.data.entity.ServiceEntry
import com.github.luciangutu.carserviceapp.data.repository.CarRepository
import java.io.File

data class DatabaseBackup(
    val version: Int,
    val cars: List<Car>,
    val serviceEntries: List<ServiceEntry>,
    val maintenanceIntervals: List<MaintenanceInterval>,
    val operationSuggestions: List<OperationSuggestion>
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: CarRepository,
    private val database: CarDatabase
) : ViewModel() {

    val appVersion: String by lazy {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    private val _exportUri = MutableSharedFlow<Uri>()
    val exportUri: SharedFlow<Uri> = _exportUri

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun exportDatabase() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val backupData = withContext(Dispatchers.IO) {
                    DatabaseBackup(
                        version = 2,
                        cars = repository.getAllCarsList(),
                        serviceEntries = repository.getAllEntriesList(),
                        maintenanceIntervals = repository.getAllIntervalsList(),
                        operationSuggestions = repository.getSuggestionsList()
                    )
                }

                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(backupData)

                val cacheFile = File(context.cacheDir, "car_service_backup.json")
                withContext(Dispatchers.IO) {
                    cacheFile.writeText(jsonString)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
                _exportUri.emit(uri)
            } catch (e: Exception) {
                _message.emit("Export failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = withContext(Dispatchers.IO) {
                    val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader().use { it.readText() }
                    }

                    if (jsonContent.isNullOrBlank()) {
                        return@withContext false
                    }

                    val gson = GsonBuilder().create()
                    val backup = gson.fromJson(jsonContent, DatabaseBackup::class.java)

                    // Clear database tables securely
                    // To handle auto-increment resetting or safely rebuilding, we populate lists.
                    // Room doesn't allow dropping inside normal transactions cleanly without closing, 
                    // but we can clear tables and re-insert elements directly.
                    database.runInTransaction {
                        // We use a coroutine launcher runBlocking or sequential clear query if available.
                        // Since we are on Dispatchers.IO, we can launch blockers or run direct SQLite queries.
                        val sdb = database.openHelper.writableDatabase
                        sdb.execSQL("DELETE FROM operation_suggestions")
                        sdb.execSQL("DELETE FROM maintenance_intervals")
                        sdb.execSQL("DELETE FROM service_entries")
                        sdb.execSQL("DELETE FROM cars")
                    }

                    // Re-insert backup entities step-by-step to respect Foreign Key requirements
                    backup.cars.forEach { repository.insertCar(it) }
                    backup.serviceEntries.forEach { repository.insertServiceEntry(it) }
                    backup.maintenanceIntervals.forEach { repository.insertMaintenanceInterval(it) }
                    backup.operationSuggestions.forEach { repository.insertOperationSuggestion(it) }
                    true
                }

                if (success) {
                    _message.emit("Database restored successfully from JSON.")
                } else {
                    _message.emit("Import failed: Empty backup file.")
                }
            } catch (e: Exception) {
                _message.emit("Import failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
