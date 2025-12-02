package cmpt362.group29.sfudining.restaurants

import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

// https://firebase.google.com/docs/firestore/query-data/get-data

data class MenuItem(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val kcal: String = ""
)

data class FeaturedItem(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val imageName: String = "",
    val kcal: String = ""
)

data class OpeningHours(
    val day: String = "",
    val hours: String = ""
)
data class Restaurant(
    val id: String = "",
    val description: String = "",
    val averagePrice: String = "",
    val name: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val cuisine: String = "",
    val schedule: List<OpeningHours> = emptyList(),
    val phoneNum: String = "",
    val address: String = "",
    val featuredItems: List<FeaturedItem> = emptyList(),
    val menu: List<MenuItem> = emptyList(),
    val restaurantImageURL: String = ""
)

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