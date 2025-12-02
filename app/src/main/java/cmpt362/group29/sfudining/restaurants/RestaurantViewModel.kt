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