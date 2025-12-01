package cmpt362.group29.sfudining.visits

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cmpt362.group29.sfudining.auth.AuthRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitPage(
    viewModel: VisitViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userId = AuthRepository().getCurrentUser()?.uid
    val initialVisit = Visit()

    VisitForm(
        visit = initialVisit,
        onSave = { visit ->
            userId?.let { viewModel.addVisit(it, visit) }
            navController.popBackStack()
        },
        onDelete = null,
        onBack = { navController.popBackStack() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailPage(
    visit: Visit,
    viewModel: VisitViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userId = AuthRepository().getCurrentUser()?.uid

    VisitForm(
        visit = visit,
        onSave = { updatedVisit ->
            userId?.let { viewModel.editVisit(it, updatedVisit) }
            navController.popBackStack()
        },
        onDelete = {
            userId?.let { viewModel.deleteVisit(it, visit.id!!) }
            navController.popBackStack()
        },
        onBack = { navController.popBackStack() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitForm(
    modifier: Modifier = Modifier,
    visit: Visit,
    onSave: (Visit) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    var restaurantName by remember { mutableStateOf(visit.restaurantName) }
    var totalCost by remember { mutableStateOf(visit.totalCost?.toString() ?: "") }
    var totalCal by remember { mutableStateOf(visit.totalCal?.toString() ?: "") }
    var comments by remember { mutableStateOf(visit.comments ?: "") }
    var date by remember { mutableStateOf(Calendar.getInstance().apply { time = visit.datetime }) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val formattedDate by remember(date.time) {
        mutableStateOf(
            SimpleDateFormat(
                "EEE, MMM d • h:mm a",
                Locale.getDefault()
            ).format(date.time)
        )
    }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    date.set(Calendar.YEAR, year)
                    date.set(Calendar.MONTH, month)
                    date.set(Calendar.DAY_OF_MONTH, day)
                    showTimePicker = true
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH)
            ).show()
            showDatePicker = false
        }
    }

    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    date.set(Calendar.HOUR_OF_DAY, hour)
                    date.set(Calendar.MINUTE, minute)
                    showTimePicker = false
                },
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visit Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    Text(
                        text = "Visit Information",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = restaurantName,
                        onValueChange = { restaurantName = it },
                        label = { Text("Restaurant Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = totalCost,
                        onValueChange = { totalCost = it },
                        label = { Text("Total Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = totalCal,
                        onValueChange = { totalCal = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Comments") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date & Time") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Cancel")
                }

                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }

                Button(onClick = {
                    val updatedVisit = visit.copy(
                        restaurantName = restaurantName,
                        totalCost = totalCost.toDoubleOrNull(),
                        totalCal = totalCal.toIntOrNull(),
                        comments = comments,
                        datetime = date.time
                    )
                    onSave(updatedVisit)
                }) {
                    Text("Save")
                }
            }
        }
    }
}
