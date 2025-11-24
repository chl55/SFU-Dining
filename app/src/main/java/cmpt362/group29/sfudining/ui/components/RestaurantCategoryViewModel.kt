package cmpt362.group29.sfudining.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cmpt362.group29.sfudining.restaurants.Restaurant
import cmpt362.group29.sfudining.restaurants.RestaurantCategory
import cmpt362.group29.sfudining.restaurants.RestaurantCategoryRepository
import cmpt362.group29.sfudining.restaurants.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RestaurantCategoryViewModel(
    private val repository: RestaurantCategoryRepository = RestaurantCategoryRepository()
) : ViewModel() {

    private val _categories = MutableStateFlow(
        value = RestaurantCategory(categories = emptyList())
    )
    val categories: StateFlow<RestaurantCategory> = _categories

    fun getCategories() {
        viewModelScope.launch {
            _categories.value = repository.getCategories()!!
        }
    }
}
