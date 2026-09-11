package com.lucian.carserviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucian.carserviceapp.ui.viewmodel.MaintenanceStatus
import com.lucian.carserviceapp.ui.viewmodel.MaintenanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceIntervalsScreen(
    carId: Long,
    onBackClick: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val statuses by viewModel.getMaintenanceStatus(carId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance Intervals") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Interval")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (statuses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No maintenance intervals defined")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statuses) { status ->
                        MaintenanceStatusCard(
                            status = status,
                            onDelete = { viewModel.deleteInterval(status.interval) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddIntervalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { component, km, years ->
                viewModel.addInterval(carId, component, km, years)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MaintenanceStatusCard(
    status: MaintenanceStatus,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val statusColor = when (status.status) {
        "Overdue" -> MaterialTheme.colorScheme.error
        "Upcoming" -> Color(0xFFFFA500)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.component,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Due: ${status.dueKm} km / ${dateFormat.format(Date(status.dueDate))}")
                Text(
                    text = "Status: ${status.status}",
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIntervalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    var component by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Interval") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = component, onValueChange = { component = it }, label = { Text("Component (e.g. Ulei)") })
                OutlinedTextField(value = km, onValueChange = { km = it }, label = { Text("Interval KM") })
                OutlinedTextField(value = years, onValueChange = { years = it }, label = { Text("Interval Years") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val kmInt = km.toIntOrNull() ?: 0
                val yearsInt = years.toIntOrNull() ?: 0
                if (component.isNotBlank()) {
                    onConfirm(component, kmInt, yearsInt)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
