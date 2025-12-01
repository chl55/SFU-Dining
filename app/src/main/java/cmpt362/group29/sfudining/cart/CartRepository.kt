package cmpt362.group29.sfudining.cart

import androidx.compose.runtime.mutableStateListOf

data class CartItem(
    val restaurantName: String,
    val title: String,
    val price: String,
    val calories: Int,
    val quantity: Int = 1
)

object CartRepository {
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems

    fun addItem(item: CartItem) {
        val index = _cartItems.indexOfFirst { it.title == item.title }
        if (index != -1) {
            val existing = _cartItems[index]
            _cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            _cartItems.add(item)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        val index = _cartItems.indexOfFirst { it.title == item.title && it.restaurantName == item.restaurantName }
        if (index != -1) {
            val existing = _cartItems[index]
            if (existing.quantity > 1) {
                _cartItems[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                _cartItems.removeAt(index)
            }
        }
    }

    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
    }

    fun clear() {
        _cartItems.clear()
    }
}