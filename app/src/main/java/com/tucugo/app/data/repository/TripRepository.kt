package com.tucugo.app.data.repository

import com.tucugo.app.data.models.Trip
import com.tucugo.app.data.models.InterstateTrip
import com.tucugo.app.data.models.Location
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class TripRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tripsCollection = db.collection("trips")
    private val interstateCollection = db.collection("interstate_trips")

    // --- Viajes Urbanos (Taxi/Moto) ---
    suspend fun createTrip(trip: Trip): Result<String> {
        return try {
            val docRef = tripsCollection.add(trip).await()
            tripsCollection.document(docRef.id).update("id", docRef.id).await()
            Result.success(docRef.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- Viajes Interurbanos (Largos) ---
    suspend fun requestInterstateTrip(trip: InterstateTrip): Result<String> {
        return try {
            val docRef = interstateCollection.add(trip).await()
            interstateCollection.document(docRef.id).update("id", docRef.id).await()
            Result.success(docRef.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getInterstateTrips(): Flow<List<InterstateTrip>> = callbackFlow {
        val subscription = interstateCollection
            .whereEqualTo("status", "pending_driver_acceptance")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(InterstateTrip::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun makeCounterOffer(tripId: String, amount: Double): Result<Unit> {
        return try {
            interstateCollection.document(tripId).update("driverCounterOffer", amount).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- Operaciones de Seguimiento ---
    fun updateDriverLocation(tripId: String, isInterstate: Boolean, lat: Double, lng: Double) {
        val collection = if (isInterstate) interstateCollection else tripsCollection
        collection.document(tripId).update("driverLocation", Location(lat = lat, lng = lng))
    }

    suspend fun completeTrip(tripId: String, isInterstate: Boolean): Result<Unit> {
        return try {
            val collection = if (isInterstate) interstateCollection else tripsCollection
            collection.document(tripId).update("status", "completed").await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
