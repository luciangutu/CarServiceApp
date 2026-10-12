package com.github.luciangutu.carserviceapp.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.luciangutu.carserviceapp.data.parseServiceOperations
import com.github.luciangutu.carserviceapp.ui.components.TagInput
import com.github.luciangutu.carserviceapp.ui.viewmodel.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceAddEditScreen(
    carId: Long,
    serviceId: Long = 0,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit,
    serviceViewModel: ServiceViewModel = hiltViewModel()
) {
    var kilometers by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedOperations by remember { mutableStateOf(emptyList<String>()) }
    var pendingOperation by remember { mutableStateOf("") }

    val serviceEntry by serviceViewModel.serviceEntry.collectAsState()

    LaunchedEffect(serviceId) {
        serviceViewModel.getServiceEntryById(serviceId)
    }

    LaunchedEffect(serviceEntry) {
        if (serviceEntry == null && serviceId == 0L) {
            kilometers = ""
            serviceName = ""
            cost = ""
            selectedDate = System.currentTimeMillis()
            selectedOperations = emptyList()
            pendingOperation = ""
        } else {
            serviceEntry?.let {
                kilometers = it.kilometers.toString()
                serviceName = it.serviceName
                cost = it.cost?.toString() ?: ""
                selectedDate = it.date
                selectedOperations = parseServiceOperations(it.operations)
            }
        }
    }

    val suggestions by serviceViewModel.suggestions.collectAsState()
    val suggestionNames = suggestions.map { it.name }

    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(year, month, dayOfMonth)
            selectedDate = newCalendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (serviceId == 0L) "Add Service" else "Edit Service") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date: ${dateFormat.format(Date(selectedDate))}")
            }

            OutlinedTextField(
                value = kilometers,
                onValueChange = { if (it.all { char -> char.isDigit() }) kilometers = it },
                label = { Text("Kilometers") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text("Service Name (e.g. Dealer, Shop)") },
                modifier = Modifier.fillMaxWidth()
            )

            TagInput(
                selectedTags = selectedOperations,
                onTagsChanged = { selectedOperations = it },
                suggestions = suggestionNames,
                onPendingTagChanged = { pendingOperation = it },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Button(
                onClick = {
                    val finalOperations = if (pendingOperation.trim().isNotEmpty() && !selectedOperations.contains(pendingOperation.trim())) {
                        selectedOperations + pendingOperation.trim()
                    } else {
                        selectedOperations
                    }
                    serviceViewModel.saveServiceEntry(
                        id = serviceId,
                        carId = carId,
                        date = selectedDate,
                        kilometers = kilometers.toIntOrNull() ?: 0,
                        serviceName = serviceName,
                        operations = finalOperations,
                        cost = cost.toDoubleOrNull(),
                        onSuccess = onSaveSuccess
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = kilometers.isNotEmpty() && serviceName.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}
