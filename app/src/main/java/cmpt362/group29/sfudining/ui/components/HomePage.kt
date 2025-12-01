package cmpt362.group29.sfudining.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cmpt362.group29.sfudining.R
import cmpt362.group29.sfudining.restaurants.Restaurant
import coil.compose.rememberAsyncImagePainter

@Composable
fun HomePage(restaurants: List<Restaurant>,
             modifier: Modifier = Modifier,
             onRestaurantClick: (String) -> Unit) {
    val categoryViewModel: RestaurantCategoryViewModel = viewModel()
    val categories by categoryViewModel.categories.collectAsState()
    LaunchedEffect(Unit) {
        categoryViewModel.getCategories()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(6.dp)
    ) {
        categories.categories.forEach { category ->
            RestaurantRow(restaurants, category, onRestaurantClick)
//            restaurants.filter{ it.cuisine == category }
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
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${restaurant.cuisine}", // Cuisines: burgers, sushi, chinese, indian, etc...
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Average Price: ${restaurant.averagePrice}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
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
private fun Preview() {
    HomePage(
        restaurants = emptyList(),
        modifier = Modifier,
        onRestaurantClick = {}
    )
}