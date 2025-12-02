package cmpt362.group29.sfudining.restaurants

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*
import android.location.Location
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object RestaurantUtils {
    fun parsePriceRange(range: String): Pair<Int, Int>? {
        return range
            .replace("$", "")
            .split("-")
            .takeIf { it.size == 2 }
            ?.mapNotNull { it.toIntOrNull() }
            ?.let { Pair(it[0], it[1]) }
    }

    @Composable
    fun OpeningStatusBadge(schedule: List<OpeningHours>) {
        val isOpen = isOpenNow(schedule)
        val closingSoon = closesWithinAnHour(schedule)

        val (label, color) = when {
            isOpen && closingSoon -> "Closing Soon" to Color(0xFFFF5722)
            isOpen -> "Open Now" to Color(0xFF4CAF50)
            else -> "Closed" to Color(0xFFE91E1E)
        }

        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.25f), shape = RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }

    suspend fun distanceToRestaurant(context: Context, location: GeoPoint): String? {
        val act = context as? ComponentActivity ?: return null

        val granted = ContextCompat.checkSelfPermission(
            act,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                act,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return null
        }

        val fused: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val userLocation = suspendCancellableCoroutine { cont ->
            fused.lastLocation.addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

        if (userLocation == null) return null

        val result = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            location.latitude,
            location.longitude,
            result
        )

        val meters = result[0]
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            "${(meters / 1000f).toString().take(4)} km"
        }
    }

    fun isOpenNow(schedule: List<OpeningHours>): Boolean {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        val currentTime = calendar.time

        schedule.forEach { entry ->
            if (!entry.day.equals(currentDay, ignoreCase = true)) return@forEach
            if (entry.hours.equals("Closed", ignoreCase = true)) return false
            if (entry.hours.equals("Open 24 Hours", ignoreCase = true)) return true

            val times = entry.hours.split("–", "-").map { it.trim() }
            if (times.size != 2) return false

            try {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val start = timeFormat.parse(times[0]) ?: return false
                val end = timeFormat.parse(times[1]) ?: return false

                val calStart = Calendar.getInstance().apply {
                    time = start
                    set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                }

                val calEnd = Calendar.getInstance().apply {
                    time = end
                    set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                }

                if (currentTime.after(calStart.time) && currentTime.before(calEnd.time)) {
                    return true
                }
            } catch (_: Exception) {
                return false
            }
        }
        return false
    }

    fun closesWithinAnHour(schedule: List<OpeningHours>): Boolean {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        val currentTime = calendar.time

        schedule.forEach { entry ->
            if (!entry.day.equals(currentDay, ignoreCase = true)) return@forEach
            if (entry.hours.equals("Open 24 Hours", ignoreCase = true)) return false
            if (entry.hours.equals("Closed", ignoreCase = true)) return false

            val times = entry.hours.split("–", "-").map { it.trim() }
            if (times.size != 2) return false

            try {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val end = timeFormat.parse(times[1]) ?: return false
                val calEnd = Calendar.getInstance().apply {
                    time = end
                    set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                }

                val diff = calEnd.time.time - currentTime.time
                return diff in 0..3600000
            } catch (_: Exception) {
                return false
            }
        }
        return false
    }
}
