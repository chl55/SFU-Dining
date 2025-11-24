package cmpt362.group29.sfudining.visits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import cmpt362.group29.sfudining.auth.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun onVisitClick(navController: NavController, visit: Visit) {
    navController.navigate("visit_detail/${visit.id}")
}

fun onAddVisitButtonClick(navController: NavHostController) {
    navController.navigate("add_visit")
}

@Composable
fun VisitPage(modifier: Modifier = Modifier, navController: NavHostController, viewModel : VisitViewModel) {
    // Get current user id
    val auth = AuthRepository()
    val userId = auth.getCurrentUser()?.uid

    LaunchedEffect(userId) {
        viewModel.loadVisits(userId!!)
    }
    val visits = viewModel.visits

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (visits.isEmpty()) {
            NoVisitsMessage()
        } else {
            VisitList(visits, navController)
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        AddVisitButton(navController)
    }

}

@Composable
fun AddVisitButton(navController: NavHostController) {
    FloatingActionButton(
        onClick = { onAddVisitButtonClick(navController) },
        containerColor = Color.Red,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
            .size(100.dp)
            .padding(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Visit",
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
    }
}

@Composable
fun NoVisitsMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No visits yet. Add one to get started!")
    }
}

@Composable
fun VisitList(visits: List<Visit>, navController: NavController) {
    val sortedVisits = visits.sortedByDescending { it.datetime }
    LazyColumn {
        items(sortedVisits) { visit ->
            VisitItemRow(visit, navController)
        }
    }
}

@Composable
fun VisitItemRow(visit: Visit, navController: NavController) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val restaurantName = visit.restaurantName.ifBlank { "Unknown Restaurant" }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onVisitClick(navController, visit) }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(restaurantName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = dateFormat.format(visit.datetime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val details = buildList {
                visit.totalCost?.let {
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                    add("Total: ${currencyFormat.format(it)}")
                }
                visit.totalCal?.let { add("$it kcal") }
            }.joinToString(" • ")

            if (details.isNotEmpty()) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}