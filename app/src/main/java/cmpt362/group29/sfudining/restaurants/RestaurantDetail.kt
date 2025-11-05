package cmpt362.group29.sfudining.restaurants

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RestaurantDetail(restaurant: Restaurant?, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = restaurant?.name ?: "Unknown Restaurant")
        Spacer(modifier = Modifier.height(100.dp))
        Button(onClick = onBack) {
            Text("Back to Map")
        }
    }

}