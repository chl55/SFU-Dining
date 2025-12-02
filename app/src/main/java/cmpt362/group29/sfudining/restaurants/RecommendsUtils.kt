package cmpt362.group29.sfudining.restaurants

import android.location.Location
import android.util.Log
import cmpt362.group29.sfudining.visits.Visit
import com.google.firebase.firestore.GeoPoint

class RecommendsUtils {
    // -- Location-based recommendations -- //
    fun getDistance(start: GeoPoint, end: GeoPoint): Float {
        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0]
    }

    /// Sort restaurants by distance from user
    fun recommendByLocation(
        userLocation: Location?,
        restaurants: List<Restaurant>
    ): List<Restaurant> {
        if (userLocation == null) return emptyList()

        val userGeo = GeoPoint(userLocation.latitude, userLocation.longitude)
        val results = restaurants.sortedBy { restaurant ->
            getDistance(userGeo, restaurant.location)
        }
        return results
    }

    // -- History-based recommendations -- //
    /// Finding user's favorite restaurants based on visit frequency
    fun recommendByHistory(
        visits: List<Visit>,
        allRestaurants: List<Restaurant>
    ): List<Restaurant> {
        if (visits.isEmpty()) return emptyList()
        val cuisineCounts = mutableMapOf<String, Int>()
        val latestVisitTime = mutableMapOf<String, Long>() // secondary sort key
        visits.forEach { visit ->
            val restaurant = allRestaurants.find { it.name.equals(visit.restaurantName, ignoreCase = true) }
            restaurant?.let {
                cuisineCounts[it.cuisine] = cuisineCounts.getOrDefault(it.cuisine, 0) + 1
                latestVisitTime[it.cuisine] = maxOf(latestVisitTime.getOrDefault(it.cuisine, 0L), visit.datetime.time)
            }
        }
        val topCuisines = cuisineCounts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }
                .thenByDescending { latestVisitTime[it.key] ?: 0L })
            .map { it.key }
            .take(3)
        return allRestaurants
            .filter { it.cuisine in topCuisines }
            .sortedWith(compareBy(
                { topCuisines.indexOf(it.cuisine) },
                { it.name.lowercase() }
            ))
    }
}
