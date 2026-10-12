package com.github.luciangutu.carserviceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.luciangutu.carserviceapp.data.entity.MaintenanceInterval
import com.github.luciangutu.carserviceapp.ui.viewmodel.MaintenanceStatus
import com.github.luciangutu.carserviceapp.ui.viewmodel.MaintenanceViewModel
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
    var editingInterval by remember { mutableStateOf<MaintenanceInterval?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance Intervals") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            onEdit = { editingInterval = status.interval },
                            onDelete = { viewModel.deleteInterval(status.interval) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MaintenanceIntervalDialog(
            onDismiss = { showAddDialog = false },
            title = "Add Maintenance Interval",
            confirmText = "Add",
            onConfirm = { component, km, years ->
                viewModel.addInterval(carId, component, km, years)
                showAddDialog = false
            }
        )
    }

    editingInterval?.let { interval ->
        MaintenanceIntervalDialog(
            interval = interval,
            onDismiss = { editingInterval = null },
            title = "Edit Maintenance Interval",
            confirmText = "Save",
            onConfirm = { component, km, years ->
                viewModel.updateInterval(interval, component, km, years)
                editingInterval = null
            }
        )
    }
}

@Composable
fun MaintenanceStatusCard(
    status: MaintenanceStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val statusColor = when (status.status) {
        "Overdue" -> MaterialTheme.colorScheme.error
        "Upcoming" -> Color(0xFFFFA500)
        else -> MaterialTheme.colorScheme.primary
    }
    val dueParts = buildList {
        if (status.interval.intervalKm > 0) {
            add("${status.dueKm} km")
        }
        status.dueDate?.let {
            add(dateFormat.format(Date(it)))
        }
    }
    val lastDoneParts = buildList {
        status.lastDoneKm?.let {
            add("$it km")
        }
        status.lastDoneDate?.let {
            add(dateFormat.format(Date(it)))
        }
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
                Text(text = "Due: ${dueParts.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "Not scheduled"}")
                Text(text = "Last done: ${lastDoneParts.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "Never"}")
                Text(
                    text = "Status: ${status.status}",
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceIntervalDialog(
    interval: MaintenanceInterval? = null,
    onDismiss: () -> Unit,
    title: String,
    confirmText: String,
    onConfirm: (String, Int, Int) -> Unit
) {
    var component by remember(interval) { mutableStateOf(interval?.component.orEmpty()) }
    var km by remember(interval) { mutableStateOf(interval?.intervalKm?.toString().orEmpty()) }
    var years by remember(interval) { mutableStateOf(interval?.intervalYears?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = component,
                    onValueChange = { component = it },
                    label = { Text("Component (e.g. Ulei)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = km,
                    onValueChange = { if (it.all { char -> char.isDigit() }) km = it },
                    label = { Text("Interval KM") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = years,
                    onValueChange = { if (it.all { char -> char.isDigit() }) years = it },
                    label = { Text("Interval Years") },
                    singleLine = true
                )
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
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
