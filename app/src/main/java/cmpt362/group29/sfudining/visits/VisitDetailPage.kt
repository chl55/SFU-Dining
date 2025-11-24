package cmpt362.group29.sfudining.visits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cmpt362.group29.sfudining.auth.AuthRepository
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitPage(
    viewModel: VisitViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var restaurantName by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var totalCal by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Date()) }
    val userId = AuthRepository().getCurrentUser()?.uid

    Scaffold { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Input fields
            OutlinedTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                label = { Text("Restaurant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = totalCost,
                onValueChange = { totalCost = it },
                label = { Text("Total Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = totalCal,
                onValueChange = { totalCal = it },
                label = { Text("Calories") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("Comments") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f)) // Push buttons to the bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }
                Button(onClick = {
                    val visit = Visit(
                        datetime = date,
                        restaurantName = restaurantName,
                        totalCost = totalCost.toDoubleOrNull(),
                        totalCal = totalCal.toIntOrNull(),
                        comments = comments
                    )
                    userId?.let { viewModel.addVisit(it, visit) }
                    navController.popBackStack()
                }) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun VisitDetailPage(
    visit: Visit,
    viewModel: VisitViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userId = AuthRepository().getCurrentUser()?.uid

    var restaurantName by remember { mutableStateOf(visit.restaurantName) }
    var totalCost by remember { mutableStateOf(visit.totalCost?.toString() ?: "") }
    var totalCal by remember { mutableStateOf(visit.totalCal?.toString() ?: "") }
    var comments by remember { mutableStateOf(visit.comments ?: "") }
    var date by remember { mutableStateOf(visit.datetime) }

    Scaffold { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                label = { Text("Restaurant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = totalCost,
                onValueChange = { totalCost = it },
                label = { Text("Total Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = totalCal,
                onValueChange = { totalCal = it },
                label = { Text("Calories") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = comments,
                onValueChange = { comments = it },
                label = { Text("Comments") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f)) // Push buttons to bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }

                Button(onClick = {
                    // Delete visit
                    userId?.let {
                        viewModel.deleteVisit(it, visit.id!!)
                        navController.popBackStack()
                    }
                }) {
                    Text("Delete")
                }

                Button(onClick = {
                    // Save changes
                    val updatedVisit = visit.copy(
                        restaurantName = restaurantName,
                        totalCost = totalCost.toDoubleOrNull(),
                        totalCal = totalCal.toIntOrNull(),
                        comments = comments,
                        datetime = date
                    )
                    userId?.let { viewModel.editVisit(it, updatedVisit) }
                    navController.popBackStack()
                }) {
                    Text("Save")
                }
            }
        }
    }
}
