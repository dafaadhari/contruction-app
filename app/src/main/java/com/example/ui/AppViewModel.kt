package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        db.projectDao(),
        db.deliveryDao(),
        db.paymentDao()
    )

    // Current navigation state: "DASHBOARD", "LOGISTICS", "BILLING"
    private val _currentTab = MutableStateFlow("DASHBOARD")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Screen-specific states (e.g. detailed project view)
    private val _selectedProjectId = MutableStateFlow<Int?>(null)
    val selectedProjectId: StateFlow<Int?> = _selectedProjectId.asStateFlow()

    // Expose repository data streams
    val projects: StateFlow<List<ConstructionProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveries: StateFlow<List<LogisticsDelivery>> = repository.allDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentRecord>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seeding disabled for clean production build with real data
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
        // Reset subscreen context when changing tabs
        if (tab != "LOGISTICS") {
            _selectedProjectId.value = null
        }
    }

    fun selectProject(projectId: Int?) {
        _selectedProjectId.value = projectId
    }

    // Financial calculations computed reactively
    // Financials by Project ID
    val projectFinancials: StateFlow<Map<Int, ProjectFinancials>> = combine(
        projects,
        deliveries,
        payments
    ) { projList, delList, payList ->
        projList.associate { project ->
            // Total delivered material value (only successful ones represent billing potential)
            val totalMaterial = delList
                .filter { it.projectId == project.id && it.status == "SUKSES" }
                .sumOf { it.quantity * it.unitPrice }

            // Total amount paid for this project (under LUNAS status)
            val totalPaid = payList
                .filter { it.projectId == project.id && it.status == "LUNAS" }
                .sumOf { it.amount }

            // Total unpaid claims / terms (under BELUM LUNAS status)
            val totalUnpaid = payList
                .filter { it.projectId == project.id && it.status == "BELUM LUNAS" }
                .sumOf { it.amount }

            project.id to ProjectFinancials(
                project = project,
                totalMaterialDeliveredValue = totalMaterial,
                totalPaidValue = totalPaid,
                totalUnpaidValue = totalUnpaid,
                remainingBillingValue = maxOf(0.0, totalMaterial - totalPaid)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Overall summary card values
    val dashboardSummary: StateFlow<DashboardSummary> = projectFinancials.combine(deliveries) { financialsMap, delList ->
        val activeProjCount = financialsMap.values.count { it.project.status == "AKTIF" }
        val pendingDeliveryCount = delList.count { it.status == "TERTUNDA" }
        val totalSisaPenagihan = financialsMap.values.sumOf { it.remainingBillingValue }

        DashboardSummary(
            activeProjects = activeProjCount,
            pendingDeliveries = pendingDeliveryCount,
            totalSisaPenagihan = totalSisaPenagihan
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary(0, 0, 0.0))

    // Deliveries grouped by day of the current week (Monday - Sunday) for chart
    val weeklyDeliveryIntensityFlag: StateFlow<List<Double>> = deliveries.map { delList ->
        // Current week boundaries
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        // Clear times
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1)
        }
        val startOfWeek = cal.timeInMillis
        cal.add(Calendar.DATE, 7)
        val endOfWeek = cal.timeInMillis

        // Group successful deliveries by day of week (Monday to Sunday)
        val daysCount = DoubleArray(7) { 0.0 } // index 0 = Mon, ..., 6 = Sun
        
        for (del in delList) {
            if (del.status == "SUKSES" && del.deliveryDateTime in startOfWeek until endOfWeek) {
                val dCal = Calendar.getInstance()
                dCal.timeInMillis = del.deliveryDateTime
                val dayOfWeek = dCal.get(Calendar.DAY_OF_WEEK) // SUNDAY=1, MONDAY=2, ...
                val index = when (dayOfWeek) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    Calendar.SUNDAY -> 6
                    else -> 0
                }
                daysCount[index] += del.quantity * del.unitPrice
            }
        }
        daysCount.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0.0 })

    // Seeding dummy data (disabled for production)
    private suspend fun seedDatabase() {
        val disabled = true
        if (disabled) return
        val proj1 = ConstructionProject(
            projectCode = "PRJ-SDR-001",
            name = "Apartemen Grand Sudirman",
            location = "Jakarta Selatan",
            status = "AKTIF",
            contractValue = 8_500_000_000.0
        )
        val proj2 = ConstructionProject(
            projectCode = "PRJ-TOL-042",
            name = "Tol Serang-Panimbang Seksi 3",
            location = "Serang, Banten",
            status = "AKTIF",
            contractValue = 24_000_000_000.0
        )
        val proj3 = ConstructionProject(
            projectCode = "PRJ-PTB-201",
            name = "Dermaga Pelabuhan Patimban",
            location = "Subang, Jawa Barat",
            status = "AKTIF",
            contractValue = 15_000_000_000.0
        )
        val proj4 = ConstructionProject(
            projectCode = "PRJ-KMP-008",
            name = "Jembatan Sungai Kampar",
            location = "Kampar, Riau",
            status = "SUKSES",
            contractValue = 3_200_000_000.0
        )

        val id1 = repository.insertProject(proj1).toInt()
        val id2 = repository.insertProject(proj2).toInt()
        val id3 = repository.insertProject(proj3).toInt()
        val id4 = repository.insertProject(proj4).toInt()

        // Timestamps calculated relative to current time to ensure they fall within the current week beautifully
        val cal = Calendar.getInstance()
        
        // Let's set some dates (Monday - Thursday of current week)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val mondayTime = cal.timeInMillis
        
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val tuesdayTime = cal.timeInMillis
        
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val wednesdayTime = cal.timeInMillis

        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val thursdayTime = cal.timeInMillis

        cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val fridayTime = cal.timeInMillis

        // Add preseed deliveries
        // 1. Grand Sudirman (ID 1)
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id1,
                materialType = "Beton Ready-Mix K-350",
                quantity = 40.0,
                unit = "m³",
                unitPrice = 1_100_000.0,
                deliveryDateTime = mondayTime,
                driverName = "Andi Wijaya",
                plateNumber = "B 9871 TQ",
                suratJalanNumber = "SJ/SDR/260608/01",
                photoPath = "MOCK_PHOTO_CONCRETE",
                status = "SUKSES"
            )
        )
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id1,
                materialType = "Pasir Pasang",
                quantity = 15.0,
                unit = "m³",
                unitPrice = 260_000.0,
                deliveryDateTime = tuesdayTime,
                driverName = "Bambang M.",
                plateNumber = "B 9012 UI",
                suratJalanNumber = "SJ/SDR/260609/02",
                photoPath = "MOCK_PHOTO_SAND",
                status = "SUKSES"
            )
        )
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id1,
                materialType = "Baja Tulangan D19",
                quantity = 12.0,
                unit = "Ton",
                unitPrice = 14_800_000.0,
                deliveryDateTime = thursdayTime, // Today
                driverName = "Surya Saputra",
                plateNumber = "B 9355 JK",
                suratJalanNumber = "SJ/SDR/260611/03",
                photoPath = "MOCK_PHOTO_STEEL",
                status = "TERTUNDA" // Show 1 pending delivery to Grand Sudirman
            )
        )

        // 2. Tol Serang (ID 2)
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id2,
                materialType = "Baja Tulangan D19",
                quantity = 18.0,
                unit = "Ton",
                unitPrice = 14_500_000.0,
                deliveryDateTime = mondayTime,
                driverName = "Budiman Santoso",
                plateNumber = "A 8021 CB",
                suratJalanNumber = "SJ/TOL/260608/11",
                photoPath = "MOCK_PHOTO_STEEL_TOL",
                status = "SUKSES"
            )
        )
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id2,
                materialType = "Semen Portland",
                quantity = 400.0,
                unit = "Zak",
                unitPrice = 82_000.0,
                deliveryDateTime = wednesdayTime,
                driverName = "Irwan Malik",
                plateNumber = "A 7110 KL",
                suratJalanNumber = "SJ/TOL/260610/12",
                photoPath = null,
                status = "SUKSES"
            )
        )
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id2,
                materialType = "Pasir Pasang",
                quantity = 60.0,
                unit = "m³",
                unitPrice = 240_000.0,
                deliveryDateTime = fridayTime, // Tomorrow
                driverName = "Dedi Hermanto",
                plateNumber = "A 9051 WE",
                suratJalanNumber = "SJ/TOL/260612/13",
                photoPath = null,
                status = "TERTUNDA" // Show 1 pending delivery to Tol Serang
            )
        )

        // 3. Dermaga Patimban (ID 3)
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id3,
                materialType = "Beton Ready-Mix K-350",
                quantity = 120.0,
                unit = "m³",
                unitPrice = 1_100_000.0,
                deliveryDateTime = wednesdayTime,
                driverName = "Joko Tarub",
                plateNumber = "E 8820 PA",
                suratJalanNumber = "SJ/PTB/260610/41",
                photoPath = "MOCK_PHOTO_CONCRETE_PTB",
                status = "SUKSES"
            )
        )

        // 4. Jembatan Sungai Kampar (ID 4 - COMPLETED)
        repository.insertDelivery(
            LogisticsDelivery(
                projectId = id4,
                materialType = "Baja Tulangan D19",
                quantity = 30.0,
                unit = "Ton",
                unitPrice = 14_000_000.0,
                deliveryDateTime = mondayTime - 7 * 86400000L, // Last week
                driverName = "Riki Subagja",
                plateNumber = "BM 8122 KK",
                suratJalanNumber = "SJ/KMP/260601/01",
                photoPath = null,
                status = "SUKSES"
            )
        )

        // Add preseed payment records (Uang yang sudah dibayar)
        // 1. Grand Sudirman: Delivered: 40*1.1M=44M + 15*260k=3.9M -> Total sukses = 47.9M. Paid: 30M. Sisa: 17.9M
        repository.insertPayment(
            PaymentRecord(
                projectId = id1,
                amount = 30_000_000.0,
                paymentDate = tuesdayTime,
                invoiceNumber = "INV/SDR/2026/01",
                notes = "DP & Pembayaran Pengiriman Tahap 1",
                status = "LUNAS"
            )
        )

        // 2. Tol Serang: Delivered: 18*14.5M=261M + 400*82k=32.8M -> Total sukses = 293.8M. Paid: 180M. Sisa: 113.8M
        repository.insertPayment(
            PaymentRecord(
                projectId = id2,
                amount = 180_000_000.0,
                paymentDate = wednesdayTime,
                invoiceNumber = "INV/TOL/2026/01",
                notes = "Termin Pembayaran Baja Tulangan",
                status = "LUNAS"
            )
        )

        // 3. Dermaga Patimban: Delivered: 120 * 1.1M = 132M. Paid: 100M. Sisa: 32M.
        repository.insertPayment(
            PaymentRecord(
                projectId = id3,
                amount = 100_000_000.0,
                paymentDate = wednesdayTime,
                invoiceNumber = "INV/PTB/2026/01",
                notes = "Uang Muka Beton Patimban",
                status = "LUNAS"
            )
        )

        // 4. Jembatan Kampar: Delivered 30*14M = 420M. Paid: 420M. Sisa: 0.
        repository.insertPayment(
            PaymentRecord(
                projectId = id4,
                amount = 420_000_000.0,
                paymentDate = mondayTime,
                invoiceNumber = "INV/KMP/2026/01",
                notes = "Otomatisasi Pelunasan Jembatan",
                status = "LUNAS"
            )
        )
    }

    // Database operation actions
    fun addNewProject(
        code: String,
        name: String,
        location: String,
        contractValue: Double,
        status: String = "AKTIF"
    ) {
        viewModelScope.launch {
            repository.insertProject(
                ConstructionProject(
                    projectCode = code,
                    name = name,
                    location = location,
                    status = status,
                    contractValue = contractValue
                )
            )
        }
    }

    fun addNewDelivery(
        projectId: Int,
        materialType: String,
        quantity: Double,
        unit: String,
        unitPrice: Double,
        driverName: String,
        plateNumber: String,
        status: String,
        photoPath: String?
    ) {
        viewModelScope.launch {
            // Generate robust Surat Jalan Number based on short date
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR) % 100
            val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
            val day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
            val randNum = String.format("%03d", (100..999).random())
            val sjNumber = "SJ/DEL/$year$month$day/$randNum"

            repository.insertDelivery(
                LogisticsDelivery(
                    projectId = projectId,
                    materialType = materialType,
                    quantity = quantity,
                    unit = unit,
                    unitPrice = unitPrice,
                    deliveryDateTime = System.currentTimeMillis(),
                    driverName = driverName,
                    plateNumber = plateNumber,
                    suratJalanNumber = sjNumber,
                    photoPath = photoPath,
                    status = status
                )
            )
        }
    }

    fun deleteDelivery(delivery: LogisticsDelivery) {
        viewModelScope.launch {
            repository.deleteDelivery(delivery)
        }
    }

    fun updateDeliveryStatus(deliveryId: Int, newStatus: String) {
        viewModelScope.launch {
            deliveries.value.find { it.id == deliveryId }?.let { original ->
                repository.updateDelivery(original.copy(status = newStatus))
            }
        }
    }

    fun addNewPayment(
        projectId: Int,
        amount: Double,
        notes: String,
        status: String = "LUNAS",
        invoiceNo: String? = null
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val randNum = String.format("%03d", (1..999).random())
            val invNumber = invoiceNo ?: "INV/PAY/$year/$randNum"

            repository.insertPayment(
                PaymentRecord(
                    projectId = projectId,
                    amount = amount,
                    paymentDate = System.currentTimeMillis(),
                    invoiceNumber = invNumber,
                    notes = notes,
                    status = status
                )
            )
        }
    }

    fun updateProjectStatus(projectId: Int, newStatus: String) {
        viewModelScope.launch {
            projects.value.find { it.id == projectId }?.let { original ->
                repository.updateProject(original.copy(status = newStatus))
            }
        }
    }

    fun updatePaymentStatus(paymentId: Int, newStatus: String) {
        viewModelScope.launch {
            payments.value.find { it.id == paymentId }?.let { original ->
                repository.updatePayment(original.copy(status = newStatus))
            }
        }
    }

    fun deletePayment(payment: PaymentRecord) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }
}

// Data holder classes for state streaming
data class ProjectFinancials(
    val project: ConstructionProject,
    val totalMaterialDeliveredValue: Double,
    val totalPaidValue: Double,
    val totalUnpaidValue: Double,
    val remainingBillingValue: Double
)

data class DashboardSummary(
    val activeProjects: Int,
    val pendingDeliveries: Int,
    val totalSisaPenagihan: Double
)

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
