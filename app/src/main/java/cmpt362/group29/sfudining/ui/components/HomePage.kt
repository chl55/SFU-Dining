package cmpt362.group29.sfudining.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cmpt362.group29.sfudining.R
import cmpt362.group29.sfudining.auth.AuthRepository
import cmpt362.group29.sfudining.restaurants.Restaurant
import cmpt362.group29.sfudining.restaurants.RestaurantViewModel
import cmpt362.group29.sfudining.visits.VisitRepository
import cmpt362.group29.sfudining.visits.VisitViewModel
import cmpt362.group29.sfudining.visits.VisitViewModelFactory
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomePage(modifier: Modifier = Modifier,
             onRestaurantClick: (String) -> Unit) {
    val categoryViewModel: RestaurantCategoryViewModel = viewModel()
    val restaurantViewModel: RestaurantViewModel = viewModel()
    val visitRepository = VisitRepository(FirebaseFirestore.getInstance())
    val visitViewModel: VisitViewModel = viewModel(
        factory = VisitViewModelFactory(visitRepository)
    )

    val context = LocalContext.current
    val categories by categoryViewModel.categories.collectAsState()
    val nearby by restaurantViewModel.nearbyRestaurants.collectAsState()
    val historyRecommendeds by restaurantViewModel.recommendedRestaurants.collectAsState()
    val restaurants by restaurantViewModel.restaurants.collectAsState()

    val visits = visitViewModel.visits
    val userEmail = AuthRepository().getUserEmail()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Prompt for users location request, should be prompted in map as well
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        categoryViewModel.getCategories()
        restaurantViewModel.getRestaurants()
        if (userEmail != null) {
            val userId = AuthRepository().getCurrentUser()?.uid
            if (userId != null) {
                visitViewModel.loadVisits(userId)
            } else {
                Log.d("Homepage", "user id not found!")
            }
        }

        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // State update for visits
    LaunchedEffect(visits.size) {
        if (visits.isNotEmpty()) {
            restaurantViewModel.generateUserRecommendations(visits)
            Log.d("Homepage visits", visits.toString())
        } else {
            Log.d("Homepage", "visits empty")
        }
    }

    LaunchedEffect(restaurants, hasLocationPermission) {
        if (hasLocationPermission) {
            val locationResult = fusedLocationClient.lastLocation
            locationResult.addOnSuccessListener { location ->
                if (location != null) {
                    restaurantViewModel.updateLocation(location)
                }
            }
        } else {
            Log.d("Homepage", "no location perms granted")
        }
    }

    val recommends = mutableListOf<Restaurant>()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(6.dp)
    ) {
        if (nearby.isNotEmpty()) {
            recommends.addAll(nearby)
        } else {
            Log.d("Homepage", "nearby empty")
        }

        if (historyRecommendeds.isNotEmpty()) {
            recommends.addAll(historyRecommendeds)
        } else {
            Log.d("Homepage", "recommends empty")
        }

        if (recommends.isNotEmpty()) {
            recommends.shuffle()
            val uniqueRecommends = recommends.distinctBy { it.id }
            RestaurantRow(uniqueRecommends, "Recommended Restaurants", onRestaurantClick)
        }

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

//@Preview(showBackground = true)
//@Composable
//private fun Preview() {
//    HomePage(
//        restaurants = emptyList(),
//        modifier = Modifier,
//        onRestaurantClick = {}
//    )
//}