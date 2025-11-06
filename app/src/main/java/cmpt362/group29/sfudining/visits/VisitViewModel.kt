package cmpt362.group29.sfudining.visits

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VisitViewModel(private val repository: VisitRepository) : ViewModel() {

    val visits = mutableStateListOf<Visit>()

    fun loadVisits(userId: String) {
        repository.getVisits(userId) { list ->
            visits.clear()
            visits.addAll(list)
        }
    }

    fun addVisit(userId: String, visit: Visit) {
        repository.addVisit(userId, visit) { success ->
            if (success) visits.add(visit)
        }
    }

    fun editVisit(userId: String, visit: Visit) {
        repository.editVisit(userId, visit) { success ->
            if (success) {
                val index = visits.indexOfFirst { it.id == visit.id }
                if (index != -1) {
                    visits[index] = visit
                }
            }
        }
    }

    fun deleteVisit(userId: String, visitId: String) {
        repository.deleteVisit(userId, visitId) { success ->
            if (success) {
                visits.removeAll { it.id == visitId }
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
