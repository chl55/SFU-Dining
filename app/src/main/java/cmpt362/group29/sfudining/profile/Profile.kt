package cmpt362.group29.sfudining.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Profile(
    email: String,
    profileViewModel: ProfileViewModel,
    onSignOutClick: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetPerVisit = profileViewModel.dailyBudget
    val caloriesPerVisit = profileViewModel.dailyCalories

    var editingBudget by remember { mutableStateOf(false) }
    var editingCalories by remember { mutableStateOf(false) }

    var showSignOut by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )

        Text(
            text = email,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { editingBudget = true }
        ) {
            Text(
                text = "Budget per visit",
                style = MaterialTheme.typography.titleMedium
            )

            if (editingBudget) {
                TextField(
                    value = budgetPerVisit?.toString() ?: "",
                    onValueChange = {
                        val value = it.takeIf { it.isNotBlank() }?.toIntOrNull()
                        profileViewModel.updateBudget(value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    trailingIcon = {
                        IconButton(onClick = { editingBudget = false }) {
                            Icon(Icons.Default.Check, contentDescription = "Done")
                        }
                    }
                )
            } else {
                Text(
                    text = budgetPerVisit?.let { "$$it" } ?: "No Limit",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Divider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { editingCalories = true }
        ) {
            Text(
                text = "Calories per visit",
                style = MaterialTheme.typography.titleMedium
            )

            if (editingCalories) {
                TextField(
                    value = caloriesPerVisit?.toString() ?: "",
                    onValueChange = {
                        val value = it.takeIf { it.isNotBlank() }?.toIntOrNull()
                        profileViewModel.updateCalories(value)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    trailingIcon = {
                        IconButton(onClick = { editingCalories = false }) {
                            Icon(Icons.Default.Check, contentDescription = "Done")
                        }
                    }
                )
            } else {
                Text(
                    text = caloriesPerVisit?.let { "$it kcal" } ?: "No Limit",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showSignOut = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }

    if (showSignOut) {
        onSignOutClick()
    }
}
