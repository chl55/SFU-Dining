package cmpt362.group29.sfudining.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmpt362.group29.sfudining.restaurants.Restaurant
import cmpt362.group29.sfudining.restaurants.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BrowseViewModel : ViewModel() {
    private val restaurantRepository = RestaurantRepository()

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCuisines = MutableStateFlow<Set<String>>(emptySet())
    val selectedCuisines = _selectedCuisines.asStateFlow()

    val cuisines = _restaurants.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    ).combine(_selectedCuisines) { restaurants, selected ->
        restaurants.map { it.cuisine }.distinct().associateWith { selected.contains(it) }
    }

    val restaurants = combine(
        searchText,
        _restaurants,
        _selectedCuisines
    ) { text, restaurants, selectedCuisines ->
        val filteredRestaurants = if (selectedCuisines.isEmpty()) {
            restaurants
        } else {
            restaurants.filter { selectedCuisines.contains(it.cuisine) }
        }
        if (text.isBlank()) {
            filteredRestaurants
        } else {
            filteredRestaurants.filter {
                it.name.contains(text, ignoreCase = true)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )


    init {
        viewModelScope.launch {
            _restaurants.value = restaurantRepository.getRestaurants()
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onCuisineSelected(cuisine: String, isSelected: Boolean) {
        val newSelected = _selectedCuisines.value.toMutableSet()
        if (isSelected) {
            newSelected.add(cuisine)
        } else {
            newSelected.remove(cuisine)
        }
        _selectedCuisines.value = newSelected
    }
}