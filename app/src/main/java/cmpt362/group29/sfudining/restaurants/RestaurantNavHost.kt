package cmpt362.group29.sfudining.restaurants

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow

data class MenuItem(
    val title: String = "",
    val description: String = "",
    val price: String = ""
)

data class FeaturedItem(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val imageName: String = ""
)

data class OpeningHours(
    val day: String = "",
    val hours: String = ""
)
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val cuisine: String = "",
    val schedule: List<OpeningHours> = emptyList(),
    val phoneNum: String = "",
    val address: String = "",
    val featuredItems: List<FeaturedItem> = emptyList(),
    val menu: List<MenuItem> = emptyList()
)

@Composable
fun RestaurantNavHost() {
    val navController = rememberNavController()
    val viewModel: RestaurantViewModel = viewModel()
    val restaurants by viewModel.restaurants.collectAsState(emptyList())
    LaunchedEffect(Unit) {
        viewModel.getRestaurants()
    }
    NavHost(navController, "map") {
        composable("map") {
            RestaurantMap(restaurants) { restaurant ->
                navController.navigate("info/${restaurant.id}")
            }
        }
        composable("info/{restaurantId}") {
            backStackEntry ->
            val restId = backStackEntry.arguments?.getString("restaurantId")
            Log.d("NavHost", "Navigating to restaurant ID: $restId")
            LaunchedEffect(restId) {
                if (restId != null) {
                    viewModel.getRestaurant(restId)
                }
            }
            val restaurant by viewModel.restaurant.collectAsState()
            restaurant?.let { id ->
                RestaurantDetailScreen(id) {
                    navController.popBackStack()
                }
            }
        }
    }
}