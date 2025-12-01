package cmpt362.group29.sfudining.visits

import java.util.Date

data class VisitItem(
    val id: String? = null,
    val itemId: String = "",
    val quantity: Int = 0,
    val itemName: String = "",
    val cost: Double? = null,
    val calories: Int? = null
)

data class Visit(
    val id: String? = null,
    val datetime: Date = Date(),
    val restaurantId: String = "",
    val restaurantName: String = "",
    val verified: Boolean = false,
    val items: List<VisitItem> = emptyList(),
    val totalCost: Double? = null,
    val totalCal: Int? = null,
    val pictures: List<String> = emptyList(),
    val comments: String? = null
)
