package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val projectDao: ProjectDao,
    private val deliveryDao: DeliveryDao,
    private val paymentDao: PaymentDao
) {
    val allProjects: Flow<List<ConstructionProject>> = projectDao.getAllProjects()
    val allDeliveries: Flow<List<LogisticsDelivery>> = deliveryDao.getAllDeliveries()
    val allPayments: Flow<List<PaymentRecord>> = paymentDao.getAllPayments()

    fun getProjectById(id: Int): Flow<ConstructionProject?> {
        return projectDao.getProjectById(id)
    }

    fun getDeliveriesForProject(projectId: Int): Flow<List<LogisticsDelivery>> {
        return deliveryDao.getDeliveriesForProject(projectId)
    }

    fun getPaymentsForProject(projectId: Int): Flow<List<PaymentRecord>> {
        return paymentDao.getPaymentsForProject(projectId)
    }

    suspend fun insertProject(project: ConstructionProject): Long {
        return projectDao.insertProject(project)
    }

    suspend fun updateProject(project: ConstructionProject) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: ConstructionProject) {
        projectDao.deleteProject(project)
    }

    suspend fun insertDelivery(delivery: LogisticsDelivery): Long {
        return deliveryDao.insertDelivery(delivery)
    }

    suspend fun updateDelivery(delivery: LogisticsDelivery) {
        deliveryDao.updateDelivery(delivery)
    }

    suspend fun deleteDelivery(delivery: LogisticsDelivery) {
        deliveryDao.deleteDelivery(delivery)
    }

    suspend fun insertPayment(payment: PaymentRecord): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun updatePayment(payment: PaymentRecord) {
        paymentDao.updatePayment(payment)
    }

    suspend fun deletePayment(payment: PaymentRecord) {
        paymentDao.deletePayment(payment)
    }
}
