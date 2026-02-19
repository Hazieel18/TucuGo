package com.tucugo.app.data.repository

import com.tucugo.app.data.models.Business
import com.tucugo.app.data.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BusinessRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val businessesCollection = db.collection("businesses")

    suspend fun getBusiness(businessId: String): Business? {
        val document = businessesCollection.document(businessId).get().await()
        return document.toObject(Business::class.java)
    }

    /**
     * Escucha los negocios en tiempo real para el Dashboard.
     */
    fun getFeaturedBusinesses(): Flow<List<Business>> = callbackFlow {
        val subscription = businessesCollection
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(Business::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun searchProducts(query: String): List<Product> {
        return try {
            FirebaseFirestore.getInstance().collectionGroup("products")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
                .toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getBusinessProducts(businessId: String) = businessesCollection.document(businessId)
        .collection("products")
        .whereEqualTo("isAvailable", true)

    suspend fun addProduct(businessId: String, product: Product): String {
        val docRef = businessesCollection.document(businessId).collection("products").document()
        val newProduct = product.copy(id = docRef.id)
        docRef.set(newProduct).await()
        return docRef.id
    }
}
