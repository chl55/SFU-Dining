package cmpt362.group29.sfudining.restaurants

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cmpt362.group29.sfudining.browse.BrowsePage
import cmpt362.group29.sfudining.cart.CartDetailScreen
import cmpt362.group29.sfudining.cart.CartViewModel
import cmpt362.group29.sfudining.ui.components.HomePage
import cmpt362.group29.sfudining.visits.Visit
import cmpt362.group29.sfudining.visits.VisitViewModel
import com.google.gson.Gson

@Composable
fun RestaurantNavHost(
    modifier: Modifier,
    startDestination: String = "map",
    visitViewModel: VisitViewModel,
    onNavigateParent: (String) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: RestaurantViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val restaurants by viewModel.restaurants.collectAsState(emptyList())
    val gson = Gson()
    LaunchedEffect(Unit) {
        viewModel.getRestaurants()
    }
    NavHost(navController, startDestination) {
        composable("map") {
            RestaurantMap(restaurants,
                onMarkerClick = { restaurant ->
                    navController.navigate("info/${restaurant.id}")
                },
                onCheckInClick = { visit: Visit? ->
                    val visitJson = visit?.let { gson.toJson(it) } ?: ""
                    onNavigateParent("add_visit/$visitJson")
                },
                modifier = modifier
            )
        }
        composable("browse_list") {
            BrowsePage(
                modifier = modifier,
                onRestaurantClick = { restaurantId ->
                    navController.navigate("info/$restaurantId")
                }
            )
        }
        composable("home_page") {
            HomePage(
                restaurantViewModel = viewModel,
                visitViewModel = visitViewModel,
                modifier = modifier,
                onRestaurantClick = { restaurantId ->
                    navController.navigate("info/$restaurantId")
                }
            )
        }
        composable("info/{restaurantId}") { backStackEntry ->
            val restId = backStackEntry.arguments?.getString("restaurantId")
            Log.d("NavHost", "Navigating to restaurant ID: $restId")
            LaunchedEffect(restId) {
                if (restId != null) {
                    viewModel.getRestaurant(restId)
                }
            }
            val restaurant by viewModel.restaurant.collectAsState()
            restaurant?.let { restaurantData ->
                RestaurantDetailScreen(restaurantData, cartViewModel, navController) {
                    navController.popBackStack()
                }
            }
        }
        composable("cart") {
            CartDetailScreen {
                navController.popBackStack()
            }
        }
    }
}