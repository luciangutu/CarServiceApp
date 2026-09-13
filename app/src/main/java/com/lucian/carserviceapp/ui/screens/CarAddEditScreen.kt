package com.lucian.carserviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucian.carserviceapp.ui.viewmodel.CarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarAddEditScreen(
    carId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: CarViewModel = hiltViewModel()
) {
    val carState by viewModel.car.collectAsState()

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }

    var makeError by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }
    var licensePlateError by remember { mutableStateOf(false) }

    LaunchedEffect(carId) {
        if (carId != -1L) {
            viewModel.getCarById(carId)
        }
    }

    LaunchedEffect(carState) {
        carState?.let { car ->
            make = car.make
            model = car.model
            year = car.year.toString()
            licensePlate = car.licensePlate
            vin = car.vin ?: ""
            notes = car.notes ?: ""
            odometer = car.odometer.toString()
        }
    }

    fun validate(): Boolean {
        makeError = make.isBlank()
        modelError = model.isBlank()
        licensePlateError = licensePlate.isBlank()
        return !makeError && !modelError && !licensePlateError
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (carId == -1L) "Add Car" else "Edit Car") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (validate()) {
                            viewModel.saveCar(
                                id = carId,
                                make = make,
                                model = model,
                                year = year.toIntOrNull() ?: 0,
                                licensePlate = licensePlate,
                                vin = vin.ifBlank { null },
                                notes = notes.ifBlank { null },
                                odometer = odometer.toIntOrNull() ?: 0,
                                onSuccess = onNavigateBack
                            )
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = make,
                onValueChange = { make = it; makeError = false },
                label = { Text("Make *") },
                modifier = Modifier.fillMaxWidth(),
                isError = makeError,
                singleLine = true
            )
            if (makeError) {
                Text(
                    text = "Make is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            OutlinedTextField(
                value = model,
                onValueChange = { model = it; modelError = false },
                label = { Text("Model *") },
                modifier = Modifier.fillMaxWidth(),
                isError = modelError,
                singleLine = true
            )
            if (modelError) {
                Text(
                    text = "Model is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            OutlinedTextField(
                value = year,
                onValueChange = { if (it.all { char -> char.isDigit() }) year = it },
                label = { Text("Year") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it; licensePlateError = false },
                label = { Text("License Plate *") },
                modifier = Modifier.fillMaxWidth(),
                isError = licensePlateError,
                singleLine = true
            )
            if (licensePlateError) {
                Text(
                    text = "License plate is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            OutlinedTextField(
                value = vin,
                onValueChange = { vin = it },
                label = { Text("VIN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = odometer,
                onValueChange = { if (it.all { char -> char.isDigit() }) odometer = it },
                label = { Text("Odometer (Current km)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validate()) {
                        viewModel.saveCar(
                            id = carId,
                            make = make,
                            model = model,
                            year = year.toIntOrNull() ?: 0,
                            licensePlate = licensePlate,
                            vin = vin.ifBlank { null },
                            notes = notes.ifBlank { null },
                            odometer = odometer.toIntOrNull() ?: 0,
                            onSuccess = onNavigateBack
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (carId == -1L) "Add Car" else "Update Car")
            }
        }
    }
}
