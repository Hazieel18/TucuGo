package com.tucugo.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tucugo.app.data.models.OrderItem
import com.tucugo.app.data.models.Product

/**
 * Repositorio del Carrito optimizado para reactividad y eficiencia de memoria.
 */
object CartRepository {
    private val _cartItems = MutableLiveData<Map<String, List<OrderItem>>>(emptyMap())
    val cartItems: LiveData<Map<String, List<OrderItem>>> = _cartItems

    fun addProduct(product: Product) {
        val currentMap = _cartItems.value?.toMutableMap() ?: mutableMapOf()
        val itemsInStore = currentMap[product.businessId]?.toMutableList() ?: mutableListOf()
        
        val existingItem = itemsInStore.find { it.productId == product.id }
        if (existingItem != null) {
            val index = itemsInStore.indexOf(existingItem)
            itemsInStore[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            itemsInStore.add(OrderItem(
                productId = product.id,
                quantity = 1,
                price = product.price
            ))
        }
        
        currentMap[product.businessId] = itemsInStore
        _cartItems.value = currentMap // Dispara la actualización de la UI
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    fun getTotalItemCount(): Int {
        return _cartItems.value?.values?.sumOf { list -> list.sumOf { it.quantity } } ?: 0
    }

    fun calculateTotal(): Double {
        var total = 0.0
        _cartItems.value?.values?.forEach { items ->
            items.forEach { total += (it.price * it.quantity) }
        }
        return total
    }
}
