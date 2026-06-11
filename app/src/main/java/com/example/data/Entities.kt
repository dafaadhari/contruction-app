package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "projects")
data class ConstructionProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectCode: String,
    val name: String,
    val location: String,
    val status: String, // "AKTIF", "SELESAI"
    val contractValue: Double // Nilai Kontrak / Anggaran
) : Serializable

@Entity(tableName = "deliveries")
data class LogisticsDelivery(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val materialType: String, // "Baja", "Semen", "Pasir", etc.
    val quantity: Double,
    val unit: String, // "Ton", "Zak", "m³", etc.
    val unitPrice: Double,
    val deliveryDateTime: Long, // timestamp
    val driverName: String,
    val plateNumber: String,
    val suratJalanNumber: String,
    val photoPath: String?, // String indicator for attached proof photo
    val status: String // "SUKSES", "TERTUNDA"
) : Serializable

@Entity(tableName = "payments")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val amount: Double,
    val paymentDate: Long, // timestamp
    val invoiceNumber: String,
    val notes: String,
    val status: String // "LUNAS", "BELUM LUNAS"
) : Serializable
