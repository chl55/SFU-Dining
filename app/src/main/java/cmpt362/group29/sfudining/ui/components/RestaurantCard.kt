package cmpt362.group29.sfudining.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cmpt362.group29.sfudining.R

//https://developer.android.com/develop/ui/compose/components/card
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCard() {
    Card(
        modifier = Modifier
            .width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            FoodImage()
            RestaurantInfo()
        }
    }
}

@Composable
private fun RestaurantInfo() {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Text(
            text = "RESTAURANT NAME",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "CUISINE TYPE - DISTANCE", // Cuisines: burgers, sushi, chinese, indian, etc...
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "AVERAGE PRICE",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FoodImage() {
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = "Restaurant Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
        contentScale = ContentScale.Crop
    )
}

// Might not use
@Composable
private fun Reviews() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = "RATING",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(14.dp),
            tint = Color(0xFFFF9800)
        )
        Text(
            text = "REVIEWS",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CardPreview() {
    RestaurantCard()
}