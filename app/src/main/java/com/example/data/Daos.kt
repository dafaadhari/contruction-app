package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ConstructionProject>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Int): Flow<ConstructionProject?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ConstructionProject): Long

    @Update
    suspend fun updateProject(project: ConstructionProject)

    @Delete
    suspend fun deleteProject(project: ConstructionProject)
}

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY deliveryDateTime DESC")
    fun getAllDeliveries(): Flow<List<LogisticsDelivery>>

    @Query("SELECT * FROM deliveries WHERE projectId = :projectId ORDER BY deliveryDateTime DESC")
    fun getDeliveriesForProject(projectId: Int): Flow<List<LogisticsDelivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: LogisticsDelivery): Long

    @Update
    suspend fun updateDelivery(delivery: LogisticsDelivery)

    @Delete
    suspend fun deleteDelivery(delivery: LogisticsDelivery)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payments WHERE projectId = :projectId ORDER BY paymentDate DESC")
    fun getPaymentsForProject(projectId: Int): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long

    @Update
    suspend fun updatePayment(payment: PaymentRecord)

    @Delete
    suspend fun deletePayment(payment: PaymentRecord)
}
