package cmpt362.group29.sfudining.restaurants

import java.text.SimpleDateFormat
import java.util.*

object RestaurantUtils {

    fun isOpenNow(schedule: List<OpeningHours>): Boolean {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        val currentTime = calendar.time

        schedule.forEach { entry ->
            if (!entry.day.equals(currentDay, ignoreCase = true)) return@forEach
            if (entry.hours.equals("Closed", ignoreCase = true)) return false

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
