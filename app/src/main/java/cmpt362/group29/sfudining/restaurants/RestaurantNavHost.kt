package cmpt362.group29.sfudining.restaurants

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class Restaurant(
    val id: String,
    val name: String,
    val latLng: LatLng
)

@Composable
fun RestaurantNavHost() {
    val navController = rememberNavController()
    val restaurants = listOf(
        Restaurant("1", "Uncle Fatih's Pizza", LatLng(49.27811494614226, -122.90997581099475)),
        Restaurant("2", "Togo Sushi SFU", LatLng(49.27803751562727, -122.90948764895847))
    )
    NavHost(navController, "map") {
        composable("map") {
            RestaurantMap(restaurants) { restaurant ->
                navController.navigate("info/${restaurant.id}")
            }
        }
        composable("info/{restaurantId}") {
            backStackEntry ->
            val restId = backStackEntry.arguments?.getString("restaurantId")
            val restaurant = restaurants.find{
                it.id == restId
            }
            restaurant?.let { id ->
                RestaurantDetail(id) {
                    navController.popBackStack()
                }
            }
        }
    }



}