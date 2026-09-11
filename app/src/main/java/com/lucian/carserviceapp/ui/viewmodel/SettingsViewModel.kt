package com.lucian.carserviceapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucian.carserviceapp.data.CarDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CarDatabase
) : ViewModel() {

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
                val dbFile = context.getDatabasePath("car_service_database")
                if (dbFile.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        dbFile
                    )
                    _exportUri.emit(uri)
                } else {
                    _message.emit("Database file not found")
                }
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
                withContext(Dispatchers.IO) {
                    val dbFile = context.getDatabasePath("car_service_database")
                    
                    // Close database before overwriting
                    database.close()
                    
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream?.copyTo(outputStream)
                        }
                    }
                }
                _message.emit("Database restored successfully. Please restart the app.")
            } catch (e: Exception) {
                _message.emit("Import failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
