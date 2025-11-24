package cmpt362.group29.sfudining.cart

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CartItem(
    val title: String,
    val price: String,
    val quantity: Int = 1
)

class CartViewModel(): ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    fun addItem(item: CartItem) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.title == item.title }
        if (index != -1) {
            val existingItem = current[index]
            current[index] = existingItem.copy(quantity = (existingItem.quantity + 1))
        }
        else {
            current.add(item)
        }
        _cartItems.value = current
    }

    fun removeItem(item: CartItem) {
        val current = _cartItems.value.toMutableList()
        current.remove(item)
        _cartItems.value = current
    }

    fun removeAllItem() {
        _cartItems.value = emptyList()
    }
}