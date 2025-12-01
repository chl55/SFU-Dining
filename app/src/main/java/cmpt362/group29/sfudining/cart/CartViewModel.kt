package cmpt362.group29.sfudining.cart

import androidx.lifecycle.ViewModel

class CartViewModel : ViewModel() {

    fun addItem(item: CartItem) = CartRepository.addItem(item)
    fun removeItem(item: CartItem) = CartRepository.removeItem(item)
    fun clear() = CartRepository.clear()
}