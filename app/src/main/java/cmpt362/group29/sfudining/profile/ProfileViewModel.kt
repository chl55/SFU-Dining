package cmpt362.group29.sfudining.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _dailyBudget = mutableStateOf<Int?>(null)
    val dailyBudget: Int? get() = _dailyBudget.value

    private val _dailyCalories = mutableStateOf<Int?>(null)
    val dailyCalories: Int? get() = _dailyCalories.value

    init {
        viewModelScope.launch {
            val settings = repository.getUserSettings()
            _dailyBudget.value = settings?.budgetPerVisit
            _dailyCalories.value = settings?.caloriesPerVisit
        }
    }

    fun updateBudget(value: Int?) {
        _dailyBudget.value = value
        viewModelScope.launch {
            repository.updateBudget(value)
        }
    }

    fun updateCalories(value: Int?) {
        _dailyCalories.value = value
        viewModelScope.launch {
            repository.updateCalories(value)
        }
    }
}
