package cmpt362.group29.sfudining.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cmpt362.group29.sfudining.R
import cmpt362.group29.sfudining.restaurants.Restaurant
import cmpt362.group29.sfudining.restaurants.RestaurantUtils
import cmpt362.group29.sfudining.restaurants.RestaurantUtils.parsePriceRange
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.text.style.TextOverflow

enum class RestaurantCategory(val displayName: String) {
    OPEN_NOW("Open Now"),
    UNDER_BUDGET("Under Budget"),
    NEARBY("Closest Nearby")
}

private fun parseDistanceToMeters(str: String): Float {
    val cleaned = str.trim().lowercase()
    return when {
        cleaned.endsWith("km") -> (cleaned.removeSuffix("km").trim().toFloatOrNull() ?: Float.MAX_VALUE) * 1000
        cleaned.endsWith("m") -> cleaned.removeSuffix("m").trim().toFloatOrNull() ?: Float.MAX_VALUE
        else -> Float.MAX_VALUE
    }
}

@Composable
fun HomePage(
    restaurants: List<Restaurant>,
    modifier: Modifier = Modifier,
    onRestaurantClick: (String) -> Unit
) {
    val context = LocalContext.current
    var distances by remember { mutableStateOf<Map<String, Float>?>(null) }

    LaunchedEffect(restaurants) {
        val map = restaurants.associate { r ->
            val rawDistance = RestaurantUtils.distanceToRestaurant(context, r.location)
            val meters = rawDistance?.let { parseDistanceToMeters(it) } ?: Float.MAX_VALUE
            r.id to meters
        }
        distances = map
    }

    val categories = listOf(
        RestaurantCategory.OPEN_NOW,
        RestaurantCategory.UNDER_BUDGET,
        RestaurantCategory.NEARBY
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(6.dp)
    ) {
        categories.forEach { category ->
            val filtered = when (category) {
                RestaurantCategory.OPEN_NOW ->
                    restaurants.filter { RestaurantUtils.isOpenNow(it.schedule) }

                RestaurantCategory.UNDER_BUDGET ->
                    restaurants.filter { r ->
                        parsePriceRange(r.averagePrice)?.first?.let { it <= 10 } ?: false
                    }

                RestaurantCategory.NEARBY ->
                    distances?.let { distMap ->
                        val sorted = restaurants.sortedBy { distMap[it.id] ?: Float.MAX_VALUE }
                        sorted
                    } ?: emptyList()
            }

            if (filtered.isNotEmpty()) {
                RestaurantRow(
                    restaurants = filtered,
                    category = category.displayName,
                    onRestaurantClick = onRestaurantClick
                )
            }
        }
    }
}

@Composable
fun RestaurantRow(
    restaurants: List<Restaurant>,
    category: String,
    onRestaurantClick: (String) -> Unit
) {
    Column{
        Text(
            text = category,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(5.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            restaurants.forEach { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = { onRestaurantClick(restaurant.id) }
                )
            }
        }
    }
}

//https://developer.android.com/develop/ui/compose/components/card
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column {
            FoodImage(restaurant.restaurantImageURL)
            RestaurantInfo(restaurant)
        }
    }
}

@Composable
private fun RestaurantInfo(restaurant: Restaurant) {
    val context = LocalContext.current
    var distance by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(restaurant) {
        distance = RestaurantUtils.distanceToRestaurant(context, restaurant.location)
    }

    Column(modifier = Modifier.padding(12.dp)) {
        // Row 1: Name
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = restaurant.cuisine,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal
            )

            RestaurantUtils.OpeningStatusBadge(restaurant.schedule)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Average Price: ${restaurant.averagePrice}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            distance?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FoodImage(restaurantImageURL: String) {
    Image(
        painter = if (restaurantImageURL.isNotEmpty()) {
            rememberAsyncImagePainter(restaurantImageURL)
        } else {
            painterResource(id = R.drawable.ic_launcher_background)
        },
        contentDescription = "Restaurant Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        contentScale = ContentScale.Crop
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    HomePage(
        restaurants = emptyList(),
        modifier = Modifier,
        onRestaurantClick = {}
    )
}