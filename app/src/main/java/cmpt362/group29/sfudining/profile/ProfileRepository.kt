package cmpt362.group29.sfudining.profile

import cmpt362.group29.sfudining.auth.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val store = Firebase.firestore
    private val authRepository = AuthRepository()

    private val usersRef
        get() = store.collection("users")

    suspend fun getUserSettings(): UserSettings? {
        val currentUser = authRepository.getCurrentUser() ?: return null
        val uid = currentUser.uid

        return try {
            val docSnapshot = usersRef.document(uid).get().await()
            docSnapshot.toObject(UserSettings::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateBudget(budget: Int?) {
        val currentUser = authRepository.getCurrentUser() ?: return
        val uid = currentUser.uid

        try {
            usersRef.document(uid).set(
                mapOf("budgetPerVisit" to budget),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateCalories(calories: Int?) {
        val currentUser = authRepository.getCurrentUser() ?: return
        val uid = currentUser.uid

        try {
            usersRef.document(uid).set(
                mapOf("caloriesPerVisit" to calories),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class UserSettings(
    val budgetPerVisit: Int? = null,
    val caloriesPerVisit: Int? = null
)
