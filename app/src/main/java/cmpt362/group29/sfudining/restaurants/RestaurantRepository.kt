package cmpt362.group29.sfudining.restaurants

import android.util.Log
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

// https://firebase.google.com/docs/firestore/query-data/get-data
class RestaurantRepository {
    private val store = Firebase.firestore
    private val restaurantRef = store.collection("restaurants")

    suspend fun getRestaurants(): List<Restaurant> {
        return try {
            val snapshot = restaurantRef.get().await()
            val list = snapshot.documents.mapNotNull {
                it.toObject(Restaurant::class.java)
            }
            list
        }
        catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRestaurant(id: String): Restaurant? {
        return try {
            val snapshot = restaurantRef.whereEqualTo("id", id).get().await()
            val obj = snapshot.documents.firstOrNull()?.toObject(Restaurant::class.java)
            obj
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}