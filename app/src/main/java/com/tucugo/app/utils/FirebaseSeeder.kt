package com.tucugo.app.utils

import com.tucugo.app.data.models.Business
import com.tucugo.app.data.models.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseSeeder {
    private val db = FirebaseFirestore.getInstance()

    suspend fun seedAll() {
        seedBusinesses()
    }

    private suspend fun seedBusinesses() {
        val businesses = listOf(
            Business(id = "b1", name = "Tucu Pizza", category = "restaurante", rif = "J-12345678-1"),
            Business(id = "b2", name = "Farmacia Central", category = "farmacia", rif = "J-12345678-2"),
            Business(id = "b3", name = "Super Tucu", category = "supermercado", rif = "J-12345678-3")
        )

        for (business in businesses) {
            db.collection("businesses").document(business.id).set(business).await()
            seedProducts(business.id)
        }
    }

    private suspend fun seedProducts(businessId: String) {
        val products = listOf(
            Product(name = "Producto 1", price = 10.0, businessId = businessId, isAvailable = true),
            Product(name = "Producto 2", price = 5.5, businessId = businessId, isAvailable = true)
        )
        
        val batch = db.batch()
        for (product in products) {
            val ref = db.collection("businesses").document(businessId).collection("products").document()
            batch.set(ref, product.copy(id = ref.id))
        }
        batch.commit().await()
    }
}
