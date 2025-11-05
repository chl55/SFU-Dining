package cmpt362.group29.sfudining.restaurants

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


data class OpeningHours(
    val day: String,
    val hours: String
)
data class Restaurant(
    val id: String,
    val name: String,
    val latLng: LatLng,
    val cuisine: String,
    val schedule: List<OpeningHours>,
    val phoneNum: String,
    val address: String
)

@Composable
fun RestaurantNavHost() {
    val navController = rememberNavController()
    val restaurants = listOf(
        Restaurant("1", "Uncle Fatih's Pizza", LatLng(49.27811494614226, -122.90997581099475),
            "Pizza",
            listOf(
                OpeningHours("Monday", "10 a.m.-11 p.m."),
                OpeningHours("Tuesday", "10 a.m.-11 p.m."),
                OpeningHours("Wednesday", "10 a.m.-11 p.m."),
                OpeningHours("Thursday", "10 a.m.-11 p.m."),
                OpeningHours("Friday", "10 a.m.-11 p.m."),
                OpeningHours("Saturday", "10 a.m.-11 p.m."),
                OpeningHours("Sunday", "10 a.m.-9 p.m.")),
                "(604)-564-6565",
                "9055 University High St Unit 108, Burnaby, BC V5A 0A7"
            )
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
                RestaurantDetailScreen(id) {
                    navController.popBackStack()
                }
            }
        }
    }



}