package com.lucian.carserviceapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucian.carserviceapp.data.entity.Car
import com.lucian.carserviceapp.ui.viewmodel.CarDashboardItem
import com.lucian.carserviceapp.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCarClick: (Long) -> Unit,
    onAddCarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val carItems by viewModel.carItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Service App") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCarClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Car")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (carItems.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                CarList(
                    carItems = carItems,
                    onCarClick = onCarClick
                )
            }
        }
    }
}

@Composable
fun CarList(
    carItems: List<CarDashboardItem>,
    onCarClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(carItems) { item ->
            CarCard(
                car = item.car,
                hasOverdue = item.hasOverdue,
                onClick = { onCarClick(item.car.id) }
            )
        }
    }
}

@Composable
fun CarCard(
    car: Car,
    hasOverdue: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${car.make} ${car.model}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "License Plate: ${car.licensePlate}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (hasOverdue) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text("Maintenance", modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No cars found",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add your first car.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
