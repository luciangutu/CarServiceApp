package com.lucian.carserviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.data.entity.ServiceEntry
import com.lucian.carserviceapp.ui.viewmodel.CarViewModel
import com.lucian.carserviceapp.ui.viewmodel.MaintenanceStatus
import com.lucian.carserviceapp.ui.viewmodel.MaintenanceViewModel
import com.lucian.carserviceapp.ui.viewmodel.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: Long,
    onAddServiceClick: (Long) -> Unit,
    onMaintenanceClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    carViewModel: CarViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    maintenanceViewModel: MaintenanceViewModel = hiltViewModel()
) {
    val car by carViewModel.car.collectAsState()
    val serviceEntries by serviceViewModel.serviceEntries.collectAsState()
    val maintenanceStatuses by maintenanceViewModel.getMaintenanceStatus(carId).collectAsState(initial = emptyList())

    LaunchedEffect(carId) {
        carViewModel.getCarById(carId)
        serviceViewModel.fetchEntriesForCar(carId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(car?.let { "${it.make} ${it.model}" } ?: "Car Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddServiceClick(carId) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            car?.let {
                CarInfoSection(it)
                HorizontalDivider()
                MaintenanceSection(
                    statuses = maintenanceStatuses,
                    onViewAllClick = { onMaintenanceClick(carId) }
                )
                HorizontalDivider()
            }
            
            if (serviceEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No service entries yet")
                }
            } else {
                ServiceEntriesList(serviceEntries)
            }
        }
    }
}

@Composable
fun MaintenanceSection(
    statuses: List<MaintenanceStatus>,
    onViewAllClick: () -> Unit
) {
    val upcoming = statuses
        .filter { it.status != "OK" }
        .take(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Maintenance Due",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onViewAllClick) {
                Text("View Intervals")
            }
        }
        
        if (upcoming.isEmpty()) {
            Text("All maintenance intervals are up to date.", style = MaterialTheme.typography.bodyMedium)
        } else {
            upcoming.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = status.component, modifier = Modifier.weight(1f))
                    Text(
                        text = status.status,
                        color = if (status.status == "Overdue") MaterialTheme.colorScheme.error else Color(0xFFFFA500),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CarInfoSection(car: Car) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${car.make} ${car.model}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "License Plate: ${car.licensePlate}",
            style = MaterialTheme.typography.bodyLarge
        )
        car.vin?.let {
            if (it.isNotBlank()) {
                Text(text = "VIN: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ServiceEntriesList(entries: List<ServiceEntry>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Service History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(entries) { entry ->
            ServiceEntryCard(entry)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceEntryCard(entry: ServiceEntry) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(entry.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateString,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${entry.kilometers} km",
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.serviceName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                entry.operations.split(",").filter { it.isNotBlank() }.forEach { op ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(op.trim()) }
                    )
                }
            }
            
            entry.cost?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cost: ${String.format("%.2f", it)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
