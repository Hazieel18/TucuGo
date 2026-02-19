package com.tucugo.app.data.repository

import com.tucugo.app.data.models.Report
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val reportsCollection = db.collection("reports")

    /**
     * Envía un nuevo reporte al sistema para revisión administrativa.
     */
    suspend fun submitReport(report: Report): Result<String> {
        return try {
            val docRef = reportsCollection.document()
            val finalReport = report.copy(
                id = docRef.id,
                createdAt = Date(),
                status = "open"
            )
            docRef.set(finalReport).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los reportes asociados a un usuario (como denunciante o denunciado).
     */
    suspend fun getReportsForUser(userId: String, asReporter: Boolean = true): List<Report> {
        val field = if (asReporter) "reporterId" else "reportedId"
        return try {
            reportsCollection.whereEqualTo(field, userId)
                .get()
                .await()
                .toObjects(Report::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
