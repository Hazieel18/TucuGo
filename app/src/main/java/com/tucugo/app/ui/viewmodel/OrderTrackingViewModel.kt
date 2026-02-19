package com.tucugo.app.ui.viewmodel

import androidx.lifecycle.*
import com.tucugo.app.data.models.Order
import com.tucugo.app.data.repository.OrderRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para el seguimiento en tiempo real de pedidos.
 * Sincronizado con OrderRepository para reactividad.
 */
class OrderTrackingViewModel(
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _orderStatus = MutableLiveData<Order?>()
    val orderStatus: LiveData<Order?> = _orderStatus

    /**
     * Inicia la escucha activa del estado del pedido desde el repositorio.
     */
    fun startTracking(orderId: String) {
        viewModelScope.launch {
            orderRepository.getOrderUpdates(orderId).collect { order ->
                _orderStatus.value = order
            }
        }
    }
}
