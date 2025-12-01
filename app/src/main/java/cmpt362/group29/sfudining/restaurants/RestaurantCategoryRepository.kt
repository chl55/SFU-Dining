package cmpt362.group29.sfudining.restaurants

import android.util.Log
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

// https://firebase.google.com/docs/firestore/query-data/get-data

data class RestaurantCategory(
    val categories: List<String> = emptyList()
)

class RestaurantCategoryRepository {
    private val store = Firebase.firestore
    private val restaurantRef = store.collection("restaurant_categories")

    suspend fun getCategories(): RestaurantCategory? {
        return try {
            val snapshot = restaurantRef.get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(RestaurantCategory::class.java)
            } else {
                null // no document found
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}