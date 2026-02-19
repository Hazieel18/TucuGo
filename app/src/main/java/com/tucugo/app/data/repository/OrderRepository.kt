package com.tucugo.app.data.repository

import com.tucugo.app.data.models.Order
import com.tucugo.app.data.models.Trip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repositorio central para la gestión de Pedidos y Viajes.
 * Maneja la persistencia en Firestore y el flujo de datos en tiempo real.
 */
class OrderRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ordersCollection = db.collection("orders")
    private val tripsCollection = db.collection("trips")

    // --- Gestión de Pedidos (Delivery) ---

    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val docRef = ordersCollection.document()
            val finalOrder = order.copy(id = docRef.id, orderDate = Date())
            docRef.set(finalOrder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Escucha las actualizaciones de un pedido específico para el seguimiento en el mapa.
     */
    fun getOrderUpdates(orderId: String): Flow<Order?> = callbackFlow {
        val subscription = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(Order::class.java))
            }
        awaitClose { subscription.remove() }
    }

    fun getOrdersByUser(userId: String): Flow<List<Order>> = callbackFlow {
        val subscription = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(Order::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    fun getUserOrders(userId: String): Flow<List<Order>> = getOrdersByUser(userId)

    fun getPendingOrdersForBusiness(businessId: String): Flow<List<Order>> = callbackFlow {
        val subscription = ordersCollection
            .whereEqualTo("businessId", businessId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(Order::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    // --- Gestión de Viajes (Motos / Carros) ---

    suspend fun requestTrip(trip: Trip): Result<String> {
        return try {
            val docRef = tripsCollection.document()
            val finalTrip = trip.copy(id = docRef.id, startTime = Date())
            docRef.set(finalTrip).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveTrip(userId: String): Flow<Trip?> = callbackFlow {
        val subscription = tripsCollection
            .whereEqualTo("userId", userId)
            .whereIn("status", listOf("requested", "accepted", "in_progress"))
            .addSnapshotListener { snapshot, _ ->
                val trip = snapshot?.toObjects(Trip::class.java)?.firstOrNull()
                trySend(trip)
            }
        awaitClose { subscription.remove() }
    }

    // --- Operaciones Comunes ---

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
