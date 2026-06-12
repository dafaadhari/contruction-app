package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.PdfPrintUtil
import com.example.data.ConstructionProject
import com.example.data.PaymentRecord
import com.example.ui.AppViewModel
import com.example.ui.ProjectFinancials
import com.example.ui.screens.InvoiceCreatorScreen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val financialsMap by viewModel.projectFinancials.collectAsState()

    var isAddingPayment by remember { mutableStateOf(false) }
    var expandedProjectDropdown by remember { mutableStateOf(false) }
    var selectedProjectForPayment by remember { mutableStateOf<ConstructionProject?>(null) }
    
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentNotesInput by remember { mutableStateOf("") }
    var paymentStatusSelected by remember { mutableStateOf("LUNAS") } // "LUNAS" or "BELUM LUNAS"
    var formMessage by remember { mutableStateOf<String?>(null) }
    var activeInvoiceProject by remember { mutableStateOf<ConstructionProject?>(null) }
    val deliveries by viewModel.deliveries.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackPure)
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title banner
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MODUL KEUANGAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = LimeNeon,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Penagihan & Sisa",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { isAddingPayment = !isAddingPayment },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAddingPayment) ColorError else CorporateBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = SharpShapes.medium,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("add_payment_toggle")
                ) {
                    Text(
                        text = if (isAddingPayment) "BATAL" else "INPUT BAYAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Conditional Add Payment Form
        if (isAddingPayment) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGrey, SharpShapes.medium),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "CATAT TERMIN PEMBAYARAN KLIEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = LimeNeon,
                            letterSpacing = 0.5.sp
                        )

                        // Project Dropdown Selector
                        Text(text = "PROYEK KLAIM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderGrey, SharpShapes.small)
                                    .background(MediumGrey)
                                    .clickable { expandedProjectDropdown = true }
                                    .padding(12.dp)
                                    .testTag("payment_project_dropdown"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedProjectForPayment?.name ?: "Pilih Proyek...",
                                    color = if (selectedProjectForPayment != null) Color.White else TextSecondary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = if (expandedProjectDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = expandedProjectDropdown,
                                onDismissRequest = { expandedProjectDropdown = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(MediumGrey)
                                    .border(1.dp, BorderGrey)
                            ) {
                                projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text("${proj.projectCode} - ${proj.name}", color = Color.White) },
                                        onClick = {
                                            selectedProjectForPayment = proj
                                            expandedProjectDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Amount form
                        Text(text = "JUMLAH YANG DIBAYAR (IDR)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        TextField(
                            value = paymentAmountInput,
                            onValueChange = { paymentAmountInput = it },
                            placeholder = { Text("Misal: 150000000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MediumGrey,
                                unfocusedContainerColor = MediumGrey,
                                focusedIndicatorColor = LimeNeon,
                                unfocusedIndicatorColor = BorderGrey,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = SharpShapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_amount_input")
                        )

                        // Notes form
                        Text(text = "CATATAN TERMIN / KETERANGAN", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        TextField(
                            value = paymentNotesInput,
                            onValueChange = { paymentNotesInput = it },
                            placeholder = { Text("Klaim Retensi / Termin Pembayaran I / Pelunasan...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MediumGrey,
                                unfocusedContainerColor = MediumGrey,
                                focusedIndicatorColor = LimeNeon,
                                unfocusedIndicatorColor = BorderGrey,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = SharpShapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_notes_input")
                        )

                        // Payment status selection
                        Text(text = "STATUS PEMBAYARAN", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (paymentStatusSelected == "LUNAS") ColorSuccess.copy(alpha = 0.15f) else MediumGrey, SharpShapes.medium)
                                    .clickable { paymentStatusSelected = "LUNAS" }
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("LUNAS", color = if (paymentStatusSelected == "LUNAS") ColorSuccess else TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (paymentStatusSelected == "BELUM LUNAS") ColorPending.copy(alpha = 0.15f) else MediumGrey, SharpShapes.medium)
                                    .clickable { paymentStatusSelected = "BELUM LUNAS" }
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BELUM LUNAS", color = if (paymentStatusSelected == "BELUM LUNAS") ColorPending else TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val proj = selectedProjectForPayment
                                val amtVal = paymentAmountInput.toDoubleOrNull()
                                if (proj != null && amtVal != null) {
                                    viewModel.addNewPayment(
                                        projectId = proj.id,
                                        amount = amtVal,
                                        notes = paymentNotesInput,
                                        status = paymentStatusSelected
                                    )
                                    formMessage = "Klaim termin pembayaran berhasil dicatat."
                                    // reset
                                    paymentAmountInput = ""
                                    paymentNotesInput = ""
                                    selectedProjectForPayment = null
                                    isAddingPayment = false
                                } else {
                                    formMessage = "Harap isi semua kolom dengan benar!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                            shape = SharpShapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("submit_payment_button")
                        ) {
                            Text("SIMPAN PEMBAYARAN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        if (formMessage != null) {
                            Text(
                                text = formMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LimeNeon,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Project Financial summaries displaying Contract, Materials delivered, Paid amount and Remaining Sisa Penagihan
        item {
            Text(
                text = "RINCIAN PERHITUNGAN PENAGIHAN PROYEK",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        items(financialsMap.values.toList()) { financial ->
            val p = financial.project
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("billing_card_${p.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                shape = SharpShapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Project info header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = p.projectCode, style = MaterialTheme.typography.labelSmall, color = LimeNeon)
                            Text(text = p.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        }
                        // Percentage progress metric based on Contract budget
                        val progressPercent = if (p.contractValue > 0) {
                            (financial.totalMaterialDeliveredValue / p.contractValue) * 100
                        } else 0.0
                        Text(
                            text = String.format("%.1f%% Progress", progressPercent),
                            style = MaterialTheme.typography.labelSmall,
                            color = CorporateBlue,
                            modifier = Modifier
                                .background(MediumGrey, SharpShapes.small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderGrey)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "NILAI KONTRAK / BUDGET:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(text = formatRupiah(p.contractValue), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TOTAL MATERIAL TERKIRIM:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(text = formatRupiah(financial.totalMaterialDeliveredValue), style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "JUMLAH SUDAH DIBAYAR:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(text = "- ${formatRupiah(financial.totalPaidValue)}", style = MaterialTheme.typography.bodyMedium, color = ColorSuccess)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = BorderGrey)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Critical metric: Sisa Penagihan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "SISA PENAGIHAN", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 1.sp)
                            Text(text = "(Material - Terbayar)", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
                        }
                        Text(
                            text = formatRupiah(financial.remainingBillingValue),
                            style = MaterialTheme.typography.titleLarge,
                            color = LimeNeon,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { activeInvoiceProject = p },
                        colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                        shape = SharpShapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("buat_invoice_btn_${p.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUAT INVOICE (REPLIKA FORMAL)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Historic invoice payments log list
        item {
            Text(
                text = "LOG RIWAYAT TAGIHAN & PEMBAYARAN",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (payments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGrey, SharpShapes.medium),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat transaksi keuangan.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(payments) { pay ->
                val p = projects.find { it.id == pay.projectId }
                val notesParts = pay.notes.split(" | ITEMS: ")
                val cleanNotes = notesParts.first()
                val serializedItems = if (notesParts.size > 1) notesParts[1] else null
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pay.invoiceNumber,
                                style = MaterialTheme.typography.labelLarge,
                                color = LimeNeon
                            )
                            
                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .background(if (pay.status == "LUNAS") ColorSuccess.copy(alpha = 0.15f) else ColorPending.copy(alpha = 0.15f), SharpShapes.small)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = pay.status,
                                    color = if (pay.status == "LUNAS") ColorSuccess else ColorPending,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = p?.name ?: "Proyek Unspecified",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Klaim Nilai: " + formatRupiah(pay.amount),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (pay.status == "LUNAS") ColorSuccess else ColorPending,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Divider(color = BorderGrey)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Keterangan: $cleanNotes", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text(
                                    text = "Tanggal: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(pay.paymentDate))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            
                            val printContext = LocalContext.current
                            IconButton(
                                onClick = {
                                    var dpPercent = 100.0
                                    var ppnPercent = 0.0
                                    var poNo = "PO/GEN-${pay.id}"
                                    if (cleanNotes.startsWith("Invoice formal")) {
                                        val dpRegex = "DP (\\d+\\.\\d+|\\d+)%".toRegex()
                                        val dpMatch = dpRegex.find(cleanNotes)
                                        if (dpMatch != null) {
                                            dpPercent = dpMatch.groupValues[1].toDoubleOrNull() ?: 100.0
                                        }
                                        val ppnRegex = "PPN (\\d+\\.\\d+|\\d+)%".toRegex()
                                        val ppnMatch = ppnRegex.find(cleanNotes)
                                        if (ppnMatch != null) {
                                            ppnPercent = ppnMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                                        }
                                        val poRegex = "(?:untuk|for) PO ([\\w\\-/]+)".toRegex()
                                        val poMatch = poRegex.find(cleanNotes)
                                        if (poMatch != null) {
                                            poNo = poMatch.groupValues[1]
                                        }
                                    }
                                    
                                    val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(pay.paymentDate))
                                    val payDeliveries = deliveries.filter { it.projectId == pay.projectId && it.status == "SUKSES" }
                                    
                                    val items = if (!serializedItems.isNullOrBlank()) {
                                        serializedItems.split(";").mapNotNull { itemStr ->
                                            val fields = itemStr.split(":")
                                            if (fields.size >= 4) {
                                                InvoiceItem(
                                                    description = fields[0],
                                                    quantity = fields[1].toDoubleOrNull() ?: 1.0,
                                                    unit = fields[2],
                                                    unitPrice = fields[3].toDoubleOrNull() ?: 0.0
                                                )
                                            } else {
                                                null
                                            }
                                        }
                                    } else {
                                        if (cleanNotes.startsWith("Invoice formal") && payDeliveries.isNotEmpty()) {
                                            payDeliveries.map {
                                                InvoiceItem(
                                                    description = it.materialType,
                                                    quantity = it.quantity,
                                                    unit = it.unit,
                                                    unitPrice = it.unitPrice
                                                )
                                            }
                                        } else {
                                            val baseAmount = pay.amount / (1 + ppnPercent / 100.0) / (dpPercent / 100.0)
                                            listOf(
                                                InvoiceItem(
                                                    description = cleanNotes,
                                                    quantity = 1.0,
                                                    unit = "Termin",
                                                    unitPrice = baseAmount
                                                )
                                            )
                                        }
                                    }
                                    
                                    val baseTotal = items.sumOf { it.total }
                                    val calculatedDpVal = baseTotal * (dpPercent / 100.0)
                                    val calculatedPpnVal = calculatedDpVal * (ppnPercent / 100.0)
                                    
                                    val html = generateInvoiceHtml(
                                        invoiceNo = pay.invoiceNumber,
                                        tanggal = formattedDate,
                                        poNo = poNo,
                                        kepadaYth = "PT. Cipta Karya Persada (Owner)",
                                        lokasi = p?.location ?: "Lokasi Proyek",
                                        items = items,
                                        dpPercent = dpPercent,
                                        dpVal = calculatedDpVal,
                                        ppnPercent = ppnPercent,
                                        ppnVal = calculatedPpnVal,
                                        totalTagihan = pay.amount
                                    )
                                    PdfPrintUtil.printHtml(printContext, html, "Invoice_${pay.invoiceNumber.replace("/", "_")}")
                                },
                                modifier = Modifier
                                    .background(MediumGrey, SharpShapes.small)
                                    .size(36.dp)
                                    .testTag("print_payment_invoice_${pay.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Cetak Invoice PDF",
                                    tint = CorporateBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Show Invoice Creator as a full overlay over this tab
    activeInvoiceProject?.let { project ->
        val projectDeliveries = deliveries.filter { it.projectId == project.id }
        InvoiceCreatorScreen(
            project = project,
            deliveries = projectDeliveries,
            onDismiss = { activeInvoiceProject = null },
            onSaveInvoice = { invoiceNo, totalTagihanDp, notes, isSettled ->
                viewModel.addNewPayment(
                    projectId = project.id,
                    amount = totalTagihanDp,
                    notes = notes,
                    status = if (isSettled) "LUNAS" else "BELUM LUNAS",
                    invoiceNo = invoiceNo
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
}
