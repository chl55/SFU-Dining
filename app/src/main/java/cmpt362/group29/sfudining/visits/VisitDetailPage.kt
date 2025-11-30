package cmpt362.group29.sfudining.visits

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cmpt362.group29.sfudining.auth.AuthRepository
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitPage(
    viewModel: VisitViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userId = AuthRepository().getCurrentUser()?.uid
    val initialVisit = Visit() // default empty visit
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

    Scaffold { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Text Fields
            OutlinedTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                label = { Text("Restaurant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = totalCost,
                onValueChange = { totalCost = it },
                label = { Text("Total Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = totalCal,
                onValueChange = { totalCal = it },
                label = { Text("Calories") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("Comments") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Date/Time picker
            Button(onClick = { showDatePicker = true }) {
                Text("Select Date & Time")
            }

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

            Spacer(Modifier.weight(1f)) // push buttons to bottom

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBack) { Text("Back") }

                onDelete?.let {
                    Button(onClick = it) { Text("Delete") }
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
                }) { Text("Save") }
            }
        }
    }
}
