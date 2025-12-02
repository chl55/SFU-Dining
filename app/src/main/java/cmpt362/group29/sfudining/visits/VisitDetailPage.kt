package cmpt362.group29.sfudining.visits

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cmpt362.group29.sfudining.auth.AuthRepository
import cmpt362.group29.sfudining.profile.ProfileViewModel
import cmpt362.group29.sfudining.restaurants.Restaurant
import cmpt362.group29.sfudining.restaurants.RestaurantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitPage(
    viewModel: VisitViewModel,
    profileViewModel: ProfileViewModel,
    navController: NavHostController,
    modifier: Modifier,
    initialVisit: Visit?
) {
    val userId = AuthRepository().getCurrentUser()?.uid
    val visit = initialVisit ?: Visit(
        datetime = Date()
    )

    val restaurantViewModel: RestaurantViewModel = viewModel()
    val restaurants by restaurantViewModel.restaurants.collectAsState()
    LaunchedEffect(Unit) {
        restaurantViewModel.getRestaurants()
    }

    VisitForm(
        modifier = modifier,
        profileViewModel = profileViewModel,
        restaurants = restaurants,
        visit = visit,
        onSave = { visit ->
            userId?.let { viewModel.addVisit(it, visit) }
            navController.popBackStack()
        },
        onDelete = null,
        onBack = { navController.popBackStack() }
    )
}

@Composable
fun VisitDetailPage(
    visit: Visit,
    viewModel: VisitViewModel,
    profileViewModel: ProfileViewModel,
    navController: NavHostController,
    modifier: Modifier
) {
    val userId = AuthRepository().getCurrentUser()?.uid

    val restaurantViewModel: RestaurantViewModel = viewModel()
    val restaurants by restaurantViewModel.restaurants.collectAsState()
    LaunchedEffect(Unit) {
        restaurantViewModel.getRestaurants()
    }

    VisitForm(
        modifier = modifier,
        restaurants = restaurants,
        visit = visit,
        profileViewModel = profileViewModel,
        onSave = { updatedVisit ->
            userId?.let { viewModel.editVisit(it, updatedVisit) }
            navController.popBackStack()
        },
        onDelete = {
            userId?.let { viewModel.deleteVisit(it, visit.id!!) }
            navController.popBackStack()
        },
        onBack = { navController.popBackStack() }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitForm(
    modifier: Modifier,
    profileViewModel: ProfileViewModel,
    restaurants: List<Restaurant>,
    visit: Visit,
    onSave: (Visit) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var restaurantName by remember { mutableStateOf(visit.restaurantName) }
    var totalCost by remember { mutableStateOf(visit.totalCost?.toString() ?: "") }
    var totalCal by remember { mutableStateOf(visit.totalCal?.toString() ?: "") }
    var comments by remember { mutableStateOf(visit.comments ?: "") }
    var date by remember { mutableStateOf(Calendar.getInstance().apply { time = visit.datetime }) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val budgetPerVisit = profileViewModel.dailyBudget
    val caloriesPerVisit = profileViewModel.dailyCalories
    val totalCostDouble = totalCost.toDoubleOrNull() ?: 0.0
    val totalCalInt = totalCal.toIntOrNull() ?: 0

    val overBudget = budgetPerVisit != null && totalCostDouble > budgetPerVisit
    val overCalories = caloriesPerVisit != null && totalCalInt > caloriesPerVisit

    val context = LocalContext.current
    val visitItems = remember { mutableStateListOf<VisitItem>().apply { addAll(visit.items) } }
    var expandedRestaurantField by remember { mutableStateOf(false) }

    // Get available items based on selected restaurant
    val selectedRestaurant = restaurants.find { it.name == restaurantName }
    val availableItems = remember(selectedRestaurant) {
        val items = mutableMapOf<String, Pair<Double, Int>>()
        selectedRestaurant?.let { r ->
            // Featured Items and Menu items will be combined
            r.featuredItems.forEach { item ->
                val price = item.price.replace("$", "").toDoubleOrNull() ?: 0.0
                val cal = item.kcal.toIntOrNull() ?: 0
                items[item.title] = Pair(price, cal)
            }
            r.menu.forEach { item ->
                val price = item.price.replace("$", "").toDoubleOrNull() ?: 0.0
                val cal = item.kcal.toIntOrNull() ?: 0
                items[item.title] = Pair(price, cal)
            }
        }
        items
    }

    val itemsTotalCost by remember {
        derivedStateOf {
            visitItems.sumOf {
                (it.cost ?: 0.0) * it.quantity
            }
        }
    }
    val itemsTotalCal by remember {
        derivedStateOf {
            visitItems.sumOf {
                (it.calories ?: 0) * it.quantity
            }
        }
    }

    var manualCostOverride by remember { mutableStateOf(totalCost.isNotBlank() && visitItems.isEmpty()) }
    var manualCalOverride by remember { mutableStateOf(totalCal.isNotBlank() && visitItems.isEmpty()) }

    LaunchedEffect(itemsTotalCost) {
        if (!manualCostOverride && visitItems.isNotEmpty()) totalCost = itemsTotalCost.toString()
    }

    LaunchedEffect(itemsTotalCal) {
        if (!manualCalOverride && visitItems.isNotEmpty()) totalCal = itemsTotalCal.toString()
    }

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

    Scaffold {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Visit Information", style = MaterialTheme.typography.titleMedium)

                    // Restaurant Name Drop-down box
                    ExposedDropdownMenuBox(
                        expanded = expandedRestaurantField,
                        onExpandedChange = { expandedRestaurantField = !expandedRestaurantField },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = restaurantName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Restaurant Name") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRestaurantField) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRestaurantField,
                            onDismissRequest = { expandedRestaurantField = false }
                        ) {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                restaurants.forEach { restaurant ->
                                    DropdownMenuItem(
                                        text = { Text(restaurant.name) },
                                        onClick = {
                                            restaurantName = restaurant.name
                                            expandedRestaurantField = false
                                            // Clear items if restaurant field changes
                                            visitItems.clear()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val displayCost =
                        if (!manualCostOverride && totalCost.isNotBlank())
                            "%.2f".format(totalCost.toDoubleOrNull() ?: 0.0)
                        else
                            totalCost
                    OutlinedTextField(
                        value = displayCost,
                        onValueChange = {
                            totalCost = it
                            manualCostOverride = true
                        },
                        label = { Text("Total Cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = totalCal,
                        onValueChange = {
                            totalCal = it
                            manualCalOverride = true
                        },
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

            Text("Ordered Items", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visitItems.forEachIndexed { index, item ->
                    VisitItemRow(
                        item = item,
                        availableItems = availableItems,
                        onUpdate = { updated ->
                            visitItems[index] = updated
                        },
                        onRemove = { visitItems.removeAt(index) }
                    )
                }

                OutlinedButton(
                    onClick = {
                        visitItems.add(
                            VisitItem(
                                itemName = "",
                                cost = 0.0,
                                calories = 0,
                                quantity = 1
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item")
                }
            }

            if (overBudget) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Over Budget",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You are over your budget per visit! Limit: $${budgetPerVisit}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (overCalories) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Over Calories",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You are over your calorie limit per visit! Limit: $caloriesPerVisit cal",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBack) { Text("Cancel") }
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                }
                Button(onClick = {
                    val updatedVisit = visit.copy(
                        restaurantName = restaurantName,
                        items = visitItems.toList(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitItemRow(
    item: VisitItem,
    availableItems: Map<String, Pair<Double, Int>>,
    onUpdate: (VisitItem) -> Unit,
    onRemove: () -> Unit
) {
    var name by remember { mutableStateOf(item.itemName) }
    var cost by remember { mutableStateOf(item.cost?.toString() ?: "0") }
    var calories by remember { mutableStateOf(item.calories?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(item.quantity) }
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            onUpdate(
                                item.copy(
                                    itemName = name,
                                    cost = cost.toDoubleOrNull(),
                                    calories = calories.toIntOrNull(),
                                    quantity = quantity
                                )
                            )
                        },
                        label = { Text("Item Name") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )

                    // Display available items for selected restaurant, update cost and calories based on item
                    if (availableItems.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableItems.keys.forEach { itemName ->
                                DropdownMenuItem(
                                    text = { Text(itemName) },
                                    onClick = {
                                        name = itemName
                                        val details = availableItems[itemName]
                                        if (details != null) {
                                            cost = details.first.toString()
                                            calories = details.second.toString()
                                        }
                                        expanded = false
                                        onUpdate(
                                            item.copy(
                                                itemName = name,
                                                cost = cost.toDoubleOrNull(),
                                                calories = calories.toIntOrNull(),
                                                quantity = quantity
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = cost,
                    onValueChange = {
                        cost = it
                        onUpdate(
                            item.copy(
                                itemName = name,
                                cost = cost.toDoubleOrNull(),
                                calories = calories.toIntOrNull(),
                                quantity = quantity
                            )
                        )
                    },
                    label = { Text("Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = {
                        calories = it
                        onUpdate(
                            item.copy(
                                itemName = name,
                                cost = cost.toDoubleOrNull(),
                                calories = calories.toIntOrNull(),
                                quantity = quantity
                            )
                        )
                    },
                    label = { Text("Calories") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            if (quantity > 1) quantity -= 1
                            onUpdate(
                                item.copy(
                                    itemName = name,
                                    cost = cost.toDoubleOrNull(),
                                    calories = calories.toIntOrNull(),
                                    quantity = quantity
                                )
                            )
                        },
                        modifier = Modifier.size(28.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("−", fontSize = 16.sp) }

                    Text(
                        quantity.toString(),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    TextButton(
                        onClick = {
                            quantity += 1
                            onUpdate(
                                item.copy(
                                    itemName = name,
                                    cost = cost.toDoubleOrNull(),
                                    calories = calories.toIntOrNull(),
                                    quantity = quantity
                                )
                            )
                        },
                        modifier = Modifier.size(28.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("+", fontSize = 16.sp) }
                }
            }
        }
    }
}