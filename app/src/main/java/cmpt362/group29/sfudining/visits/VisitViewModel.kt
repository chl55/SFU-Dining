package cmpt362.group29.sfudining.visits

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VisitViewModel(private val repository: VisitRepository) : ViewModel() {

    private val _visits = MutableStateFlow<List<Visit>>(emptyList())
    val visits: StateFlow<List<Visit>> = _visits

    fun loadVisits(userId: String) {
        repository.getVisits(userId) { list ->
            _visits.value = list
        }
    }

    fun addVisit(userId: String, visit: Visit) {
        // No need to manually update _visits; rely on Firestore snapshot updates
        repository.addVisit(userId, visit)
    }

    fun editVisit(userId: String, visit: Visit) {
        repository.editVisit(userId, visit) { success ->
            if (success) {
                _visits.value = _visits.value.map {
                    if (it.id == visit.id) visit else it
                }
            }
        }
    }

    fun deleteVisit(userId: String, visitId: String) {
        repository.deleteVisit(userId, visitId) { success ->
            if (success) {
                _visits.value = _visits.value.filter { it.id != visitId }
            }
        }
    }
}

class VisitViewModelFactory(private val repository: VisitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
