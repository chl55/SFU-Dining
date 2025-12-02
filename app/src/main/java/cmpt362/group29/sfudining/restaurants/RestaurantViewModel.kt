package cmpt362.group29.sfudining.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmpt362.group29.sfudining.visits.Visit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Location
import android.util.Log

class RestaurantViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants

    private val _restaurant = MutableStateFlow<Restaurant?>(null)
    val restaurant: StateFlow<Restaurant?> = _restaurant

    private val _nearbyRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val nearbyRestaurants: StateFlow<List<Restaurant>> = _nearbyRestaurants

    private val _recommendedRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val recommendedRestaurants: StateFlow<List<Restaurant>> = _recommendedRestaurants

    private val recommendsUtils = RecommendsUtils()

    fun updateLocation(location: Location?) {
        val currentList = _restaurants.value
        if (currentList.isNotEmpty() && location != null) {
            _nearbyRestaurants.value = recommendsUtils.recommendByLocation(location, currentList).take(3)
        }
    }

    fun generateUserRecommendations(visits: List<Visit>) {
        val currentRestaurants = _restaurants.value
        if (currentRestaurants.isNotEmpty() && visits.isNotEmpty()) {
            _recommendedRestaurants.value = recommendsUtils.recommendByHistory(visits, currentRestaurants)
        } else {
            Log.d("Homepage generate user recommends", "${currentRestaurants.isNotEmpty()} ${visits.isNotEmpty()}")
        }
    }

    fun getRestaurants() {
        viewModelScope.launch {
            _restaurants.value = repository.getRestaurants()
        }
    }

    fun getRestaurant(id: String) {
        viewModelScope.launch {
            _restaurant.value = repository.getRestaurant(id)!!

        }
    }

}