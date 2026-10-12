package com.github.luciangutu.carserviceapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.luciangutu.carserviceapp.data.parseServiceOperations
import com.github.luciangutu.carserviceapp.data.entity.Car
import com.github.luciangutu.carserviceapp.data.entity.ServiceEntry
import com.github.luciangutu.carserviceapp.ui.viewmodel.CarViewModel
import com.github.luciangutu.carserviceapp.ui.viewmodel.MaintenanceStatus
import com.github.luciangutu.carserviceapp.ui.viewmodel.MaintenanceViewModel
import com.github.luciangutu.carserviceapp.ui.viewmodel.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: Long,
    onAddServiceClick: (Long) -> Unit,
    onEditServiceClick: (Long, Long) -> Unit,
    onEditCarClick: (Long) -> Unit,
    onMaintenanceClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    carViewModel: CarViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    maintenanceViewModel: MaintenanceViewModel = hiltViewModel()
) {
    val car by carViewModel.car.collectAsState()
    val serviceEntries by serviceViewModel.filteredServiceEntries.collectAsState()
    val searchQuery by serviceViewModel.searchQuery.collectAsState()
    val isImporting by serviceViewModel.isImporting.collectAsState()
    val maintenanceStatuses by maintenanceViewModel.getMaintenanceStatus(carId).collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { serviceViewModel.importServiceEntries(carId, it) }
        }
    )

    var showDeleteCarDialog by remember { mutableStateOf(false) }

    if (showDeleteCarDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCarDialog = false },
            title = { Text("Delete Car?") },
            text = { Text("Are you sure you want to delete this car? All associated service history and maintenance intervals will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteCarDialog = false
                        carViewModel.deleteCar {
                            onBackClick()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCarDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(carId) {
        carViewModel.getCarById(carId)
        serviceViewModel.fetchEntriesForCar(carId)
    }

    LaunchedEffect(Unit) {
        serviceViewModel.message.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(car?.let { "${it.make} ${it.model}" } ?: "Service History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditCarClick(carId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Car")
                    }
                    IconButton(onClick = { showDeleteCarDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Car")
                    }
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = {
                                importLauncher.launch(
                                    arrayOf("text/*", "text/csv", "application/vnd.ms-excel", "*/*")
                                )
                            }
                        ) {
                            Icon(ImportIcon, contentDescription = "Import service history")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddServiceClick(carId) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            car?.let {
                MaintenanceSection(
                    statuses = maintenanceStatuses,
                    onViewAllClick = { onMaintenanceClick(carId) }
                )
                HorizontalDivider()

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = serviceViewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = {
                        Text(
                            text = "Search services or operations",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
            }
            
            if (serviceEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isNotEmpty()) "No services matching \"$searchQuery\"" else "No service entries yet")
                }
            } else {
                ServiceEntriesList(
                    entries = serviceEntries,
                    onEditClick = { entry -> onEditServiceClick(carId, entry.id) },
                    onDeleteClick = serviceViewModel::deleteServiceEntry
                )
            }
        }
    }
}

private val ImportIcon: ImageVector
    get() {
        if (_importIcon != null) return _importIcon!!
        _importIcon = ImageVector.Builder(
            name = "ImportIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
                moveTo(5f, 20f)
                horizontalLineTo(19f)
                curveTo(20.1f, 20f, 21f, 19.1f, 21f, 18f)
                verticalLineTo(15f)
                horizontalLineTo(19f)
                verticalLineTo(18f)
                horizontalLineTo(5f)
                verticalLineTo(15f)
                horizontalLineTo(3f)
                verticalLineTo(18f)
                curveTo(3f, 19.1f, 3.9f, 20f, 5f, 20f)
                close()
                moveTo(13f, 4f)
                horizontalLineTo(11f)
                verticalLineTo(12.2f)
                lineTo(7.4f, 8.6f)
                lineTo(6f, 10f)
                lineTo(12f, 16f)
                lineTo(18f, 10f)
                lineTo(16.6f, 8.6f)
                lineTo(13f, 12.2f)
                verticalLineTo(4f)
                close()
            }
        }.build()
        return _importIcon!!
    }

private var _importIcon: ImageVector? = null

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
fun ServiceEntriesList(
    entries: List<ServiceEntry>,
    onEditClick: (ServiceEntry) -> Unit,
    onDeleteClick: (ServiceEntry) -> Unit
) {
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
            ServiceEntryCard(
                entry = entry,
                onEditClick = { onEditClick(entry) },
                onDeleteClick = { onDeleteClick(entry) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceEntryCard(
    entry: ServiceEntry,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(entry.date))
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete service entry?") },
            text = { Text("This service record will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit service")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete service")
                    }
                }
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
                parseServiceOperations(entry.operations).forEach { op ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = op.trim(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
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
