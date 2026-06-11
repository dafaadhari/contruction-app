package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConstructionProject
import com.example.data.LogisticsDelivery
import com.example.ui.components.RayyanKaryaLogo
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Dynamic Material Item structure for real-time invoice generation
data class InvoiceItem(
    val id: String = UUID.randomUUID().toString(),
    var description: String,
    var quantity: Double,
    var unit: String,
    var unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreatorScreen(
    project: ConstructionProject,
    deliveries: List<LogisticsDelivery>,
    onDismiss: () -> Unit,
    onSaveInvoice: (invoiceNo: String, totalTagihanDp: Double, notes: String, isSettled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Auto-generate starting invoice numbers
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val randomSuffix = String.format("%03d", (1..999).random())
    var invoiceNo by remember { mutableStateOf("INV/PT-RK/${project.projectCode.split("-").last()}/$currentYear/$randomSuffix") }
    
    var tanggalInput by remember { 
        mutableStateOf(SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())) 
    }
    
    var poNo by remember { mutableStateOf("PO/${project.projectCode.split("-").last()}/$randomSuffix") }
    var kepadaYth by remember { mutableStateOf("PT. Cipta Karya Persada (Owner)") }
    var lokasiInput by remember { mutableStateOf(project.location) }
    
    var checkIsSettled by remember { mutableStateOf(false) }
    
    // Pre-seed invoice items from 'SUKSES' deliveries of this project
    val initialItems = remember {
        val filtered = deliveries.filter { it.projectId == project.id && it.status == "SUKSES" }
        if (filtered.isEmpty()) {
            mutableStateListOf(
                InvoiceItem(description = "Baja Ringan Truss C75 t.0.75mm", quantity = 150.0, unit = "Batang", unitPrice = 95000.0),
                InvoiceItem(description = "Reng Atap Baja Ringan t.0.45mm", quantity = 220.0, unit = "Batang", unitPrice = 45000.0),
                InvoiceItem(description = "Genteng Metal Pasir Minimalis (Blue)", quantity = 400.0, unit = "Lembar", unitPrice = 38000.0),
                InvoiceItem(description = "Aluminium Foil Double Woven Bubble", quantity = 4.0, unit = "Roll", unitPrice = 550000.0)
            )
        } else {
            val list = filtered.map {
                InvoiceItem(
                    description = it.materialType,
                    quantity = it.quantity,
                    unit = it.unit,
                    unitPrice = it.unitPrice
                )
            }
            val flowList = mutableStateListOf<InvoiceItem>()
            flowList.addAll(list)
            flowList
        }
    }

    // Modal adding manual row controls
    var showAddRowDialog by remember { mutableStateOf(false) }
    var dialogDesc by remember { mutableStateOf("") }
    var dialogQty by remember { mutableStateOf("") }
    var dialogUnit by remember { mutableStateOf("") }
    var dialogPrice by remember { mutableStateOf("") }

    // Digital Signature Pad paths
    val signaturePaths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    
    // State for input DP % and PPN %
    var dpPercentageInput by remember { mutableStateOf("50") }
    var ppnPercentageInput by remember { mutableStateOf("12") }

    // Values and calculations
    val totalContractDasar = initialItems.sumOf { it.total }
    val dpPercentage = dpPercentageInput.toDoubleOrNull() ?: 0.0
    val ppnPercentage = ppnPercentageInput.toDoubleOrNull() ?: 0.0

    val dpValue = totalContractDasar * (dpPercentage / 100.0)
    val ppnValue = if (ppnPercentage == 0.0) 0.0 else dpValue * (ppnPercentage / 100.0)
    val totalTagihanDp = dpValue + ppnValue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPure)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGrey)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.border(1.dp, BorderGrey, SharpShapes.small)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Column {
                    Text(text = "SI-RAYYAN CREATOR INVOICE", style = MaterialTheme.typography.labelSmall, color = CorporateBlue)
                    Text(text = "Faktur Formal Digital", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            
            Button(
                onClick = {
                    if (invoiceNo.isEmpty() || kepadaYth.isEmpty()) {
                        Toast.makeText(context, "Nomor Invoice & Penerima harus diisi!", Toast.LENGTH_SHORT).show()
                    } else {
                        onSaveInvoice(
                            invoiceNo,
                            totalTagihanDp,
                            "Invoice formal DP $dpPercentageInput% + PPN $ppnPercentageInput% untuk PO $poNo, Lokasi: $lokasiInput",
                            checkIsSettled
                        )
                        Toast.makeText(context, "Invoice berhasil disimpan dan diintegrasikan ke keuangan!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue),
                shape = SharpShapes.small,
                modifier = Modifier.testTag("save_invoice_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "KIRIM & SIMPAN", color = Color.Black, style = MaterialTheme.typography.labelSmall)
            }
        }

        // Layout Split: For high precision inputs on one side, and live PDF-replica preview on the other
        Row(modifier = Modifier.fillMaxSize()) {
            
            // SIDE 1: Interaktif Entry Forms (Width 360dp)
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .background(DarkGrey)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "METADATA DOKUMEN", style = MaterialTheme.typography.titleSmall, color = Color.White)
                
                // Invoice No Input
                TextField(
                    value = invoiceNo,
                    onValueChange = { invoiceNo = it },
                    label = { Text("No. Invoice") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth()
                )

                // PO No Input
                TextField(
                    value = poNo,
                    onValueChange = { poNo = it },
                    label = { Text("No. PO Pembelian") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tanggal Input
                TextField(
                    value = tanggalInput,
                    onValueChange = { tanggalInput = it },
                    label = { Text("Tanggal Invoice") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth()
                )

                // Client Name Input
                TextField(
                    value = kepadaYth,
                    onValueChange = { kepadaYth = it },
                    label = { Text("Kepada Yth. (Pelanggan)") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth()
                )

                // Lokasi Proyek Input
                TextField(
                    value = lokasiInput,
                    onValueChange = { lokasiInput = it },
                    label = { Text("Lokasi Pengiriman") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth()
                )

                // DP Percentage Input
                TextField(
                    value = dpPercentageInput,
                    onValueChange = { dpPercentageInput = it },
                    label = { Text("Persentase DP (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth().testTag("dp_percentage_input")
                )

                // PPN Percentage Input
                TextField(
                    value = ppnPercentageInput,
                    onValueChange = { ppnPercentageInput = it },
                    label = { Text("Persentase PPN (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MediumGrey,
                        unfocusedContainerColor = MediumGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = CorporateBlue
                    ),
                    shape = SharpShapes.small,
                    modifier = Modifier.fillMaxWidth().testTag("ppn_percentage_input")
                )

                HorizontalDivider(color = BorderGrey, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "RABA & MATERIAL", style = MaterialTheme.typography.titleSmall, color = Color.White)
                    IconButton(
                        onClick = { showAddRowDialog = true },
                        modifier = Modifier
                            .background(CorporateBlue, SharpShapes.small)
                            .size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }

                // Inline quick list edits
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    initialItems.forEachIndexed { idx, item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MediumGrey),
                            modifier = Modifier.fillMaxWidth(),
                            shape = SharpShapes.small
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { initialItems.removeAt(idx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ColorError, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextField(
                                        value = item.quantity.toString(),
                                        onValueChange = { qty ->
                                            val q = qty.toDoubleOrNull() ?: 1.0
                                            initialItems[idx] = item.copy(quantity = q)
                                        },
                                        label = { Text("Qty", fontSize = 9.sp) },
                                        colors = TextFieldDefaults.colors(focusedContainerColor = DarkGrey, unfocusedContainerColor = DarkGrey, focusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                        shape = SharpShapes.small
                                    )
                                    TextField(
                                        value = item.unit,
                                        onValueChange = { u ->
                                            initialItems[idx] = item.copy(unit = u)
                                        },
                                        label = { Text("Satuan", fontSize = 9.sp) },
                                        colors = TextFieldDefaults.colors(focusedContainerColor = DarkGrey, unfocusedContainerColor = DarkGrey, focusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                        shape = SharpShapes.small
                                    )
                                    TextField(
                                        value = item.unitPrice.toLong().toString(),
                                        onValueChange = { priceStr ->
                                            val p = priceStr.toDoubleOrNull() ?: 0.0
                                            initialItems[idx] = item.copy(unitPrice = p)
                                        },
                                        label = { Text("Harga", fontSize = 9.sp) },
                                        colors = TextFieldDefaults.colors(focusedContainerColor = DarkGrey, unfocusedContainerColor = DarkGrey, focusedTextColor = Color.White),
                                        modifier = Modifier.weight(1.8f),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                        shape = SharpShapes.small
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderGrey, thickness = 1.dp)

                Text(text = "Opsi Pembayaran", style = MaterialTheme.typography.titleSmall, color = Color.White)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkIsSettled = !checkIsSettled }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = checkIsSettled,
                        onCheckedChange = { checkIsSettled = it },
                        colors = CheckboxDefaults.colors(checkedColor = CorporateBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tandai Langsung Lunas (Sudah Dibayar)", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }

            VerticalDivider(color = BorderGrey, modifier = Modifier.fillMaxHeight().width(1.dp))

            // SIDE 2: LIVE PAPER INVOICE DOC PREVIEW (Crisp white sheet paper resembling actual print/PDF)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BlackPure)
                    .padding(20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // simulated standard A4 size paper container
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .border(1.dp, BorderGrey),
                    colors = CardDefaults.cardColors(containerColor = Color.White), // STAYS RIGIDLY WHITE
                    shape = SharpShapes.small
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // 1. HEADER SECTION
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    text = "PT. RAYYAN KARYA",
                                    color = Color(0xFF1C3A5E),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Supplier Atap Baja Ringan, Aluminium, dan Material Konstruksi",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                )
                                Text(
                                    text = "Email: cvrayyanalumunium@gmail.com | Hubungi: 081381080745",
                                    color = Color.DarkGray,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.1.sp
                                )
                            }
                            
                            // Logo on the right
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.weight(0.8f)
                            ) {
                                RayyanKaryaLogo(iconSize = 36.dp, showText = false, isDarkBg = false)
                            }
                        }

                        // Thick separating line representing corporate documents standard
                        Spacer(modifier = Modifier.height(12.dp))
                        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                            drawLine(
                                color = Color(0xFF1C3A5E),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 5f
                            )
                            drawLine(
                                color = Color.Black,
                                start = Offset(0f, 6f),
                                end = Offset(size.width, 6f),
                                strokeWidth = 2f
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Large centered INVOICE document title
                        Text(
                            text = "INVOICE",
                            color = Color.Black,
                            style = MaterialTheme.typography.displaySmall,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            letterSpacing = 3.sp,
                            textDecoration = TextDecoration.Underline
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. METADATA SECTION & FOR CLIENTS block (Two column row styled in strict tabular borders)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Column Left: Invoice IDs
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color.LightGray)
                                    .padding(10.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "No. Invoice:", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                    Text(text = invoiceNo, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "Tanggal:", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                    Text(text = tanggalInput, color = Color.Black, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "No. PO:", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                    Text(text = poNo, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Column Right: Customer block
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .border(1.dp, Color.LightGray)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Kepada Yth:",
                                    color = Color.DarkGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = kepadaYth,
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lokasi Pengiriman:",
                                    color = Color.DarkGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lokasiInput,
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3. TABLE OF MATERIALS DELIVERED (Replica custom layout with strict thin borders)
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1C3A5E))
                                .border(1.dp, Color.Black)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "No", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                            Text(text = "Deskripsi Barang / Material", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(text = "Jumlah", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                            Text(text = "Satuan", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                            Text(text = "Harga (Rp)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                            Text(text = "Total (Rp)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(90.dp))
                        }

                        // Items rows
                        if (initialItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Black)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "[Belum ada item ditambahkan]", color = Color.Gray, fontStyle = FontStyle.Italic, fontSize = 11.sp)
                            }
                        } else {
                            initialItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.LightGray)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${index + 1}", color = Color.Black, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(24.dp))
                                    Text(text = item.description, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(text = "${item.quantity}", color = Color.Black, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                                    Text(text = item.unit, color = Color.Black, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                                    Text(text = formatRupiahLocal(item.unitPrice), color = Color.Black, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                                    Text(text = formatRupiahLocal(item.total), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(90.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. FINANCIAL SUMMARY BLOCK & TERBILANG conversion (Left column = Terbilang, Right column = Calculations)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Column Left: Terbilang spelling
                            Column(modifier = Modifier.weight(1.2f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CorporateBlue)
                                        .background(CorporateBlue.copy(alpha = 0.05f))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "TERBILANG:",
                                            color = CorporateBlue,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "# ${angkaToTerbilang(totalTagihanDp)} #",
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontStyle = FontStyle.Italic,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Payment terms hardcoded
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray),
                                    shape = SharpShapes.small
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "INFO PEMBAYARAN:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Metode Pembayaran: Bank BCA A/N Supriyadi A/C 2453343316",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Column Right: Totals
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .border(1.dp, Color.LightGray)
                            ) {
                                // Baris 1: TOTAL NILAI KONTRAK DASAR
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "TOTAL NILAI KONTRAK DASAR:", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = formatRupiahLocal(totalContractDasar), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                // Baris 2: NILAI (DP) {X}%  - Latar abu-abu sangat muda/krem
                                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFAFAFA))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "NILAI (DP) $dpPercentageInput%:", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = formatRupiahLocal(dpValue), color = Color(0xFF1E88E5), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                                // Baris 3: PPN {Y}%
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "PPN $ppnPercentageInput%:", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = formatRupiahLocal(ppnValue), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Baris 4: TOTAL TAGIHAN DP (HARUS DIBAYAR) - Latar biru sangat muda
                                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE6F4FF))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "TOTAL TAGIHAN DP (HARUS DIBAYAR):",
                                        color = Color(0xFF1C3A5E),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                     )
                                     Spacer(modifier = Modifier.height(2.dp))
                                     Text(
                                         text = formatRupiahLocal(totalTagihanDp),
                                         color = Color.Black,
                                         fontSize = 13.sp,
                                         fontWeight = FontWeight.Black,
                                         textAlign = TextAlign.End,
                                         modifier = Modifier.fillMaxWidth()
                                     )
                                 }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // 5. SIGNATURE FIELD (Interactive finger drawing canvas pad)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(text = "Catatan Kantor:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text(text = "• Seluruh barang tetap menjadi milik PT. RAYYAN KARYA\n  hingga lunas diselesaikan.", fontSize = 8.sp, color = Color.DarkGray)
                                Text(text = "• Dokumen diterbitkan secara sah lewat sistem digital.", fontSize = 8.sp, color = Color.DarkGray)
                            }
                            
                            // Signature frame with signature pad inside
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Hormat Kami,",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "PT. RAYYAN KARYA",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                
                                // Clean interactive fingerprint/drawing canvas frame for live signatures
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(width = 160.dp, height = 75.dp)
                                        .border(0.5.dp, Color.LightGray)
                                        .background(Color(0xFFFCFCFC))
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    val path = Path().apply { moveTo(offset.x, offset.y) }
                                                    signaturePaths.add(path)
                                                    currentPath = path
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    currentPath?.lineTo(change.position.x, change.position.y)
                                                    // Force recompose of list
                                                    if (signaturePaths.isNotEmpty()) {
                                                        val last = signaturePaths.last()
                                                        signaturePaths[signaturePaths.size - 1] = last
                                                    }
                                                },
                                                onDragEnd = {
                                                    currentPath = null
                                                }
                                            )
                                        }
                                ) {
                                    // Live signature paths
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        signaturePaths.forEach { path ->
                                            drawPath(
                                                path = path,
                                                color = Color(0xFF1E4680), // Midnight corporate ink
                                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                            )
                                        }
                                        
                                    }
                                    
                                    if (signaturePaths.isEmpty()) {
                                        Text(
                                            text = "[ Tanda Tangan Kantor ]",
                                            color = Color.LightGray,
                                            fontSize = 9.sp,
                                            modifier = Modifier.align(Alignment.Center),
                                            fontStyle = FontStyle.Italic
                                        )
                                    } else {
                                        // Clear button inside signature
                                        IconButton(
                                            onClick = { signaturePaths.clear() },
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Administrasi & Keuangan",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue for manual material additions
    if (showAddRowDialog) {
        AlertDialog(
            onDismissRequest = { showAddRowDialog = false },
            title = { Text("Tambah Baris Material Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = dialogDesc,
                        onValueChange = { dialogDesc = it },
                        label = { Text("Deskripsi Barang / Material") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = dialogQty,
                            onValueChange = { dialogQty = it },
                            label = { Text("Jumlah") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        TextField(
                            value = dialogUnit,
                            onValueChange = { dialogUnit = it },
                            label = { Text("Satuan") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TextField(
                        value = dialogPrice,
                        onValueChange = { dialogPrice = it },
                        label = { Text("Harga Satuan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = dialogQty.toDoubleOrNull() ?: 1.0
                        val price = dialogPrice.toDoubleOrNull() ?: 0.0
                        if (dialogDesc.isNotEmpty()) {
                            initialItems.add(
                                InvoiceItem(
                                    description = dialogDesc,
                                    quantity = qty,
                                    unit = dialogUnit.ifEmpty { "Pcs" },
                                    unitPrice = price
                                )
                            )
                            // Clear
                            dialogDesc = ""
                            dialogQty = ""
                            dialogUnit = ""
                            dialogPrice = ""
                            showAddRowDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CorporateBlue)
                ) {
                    Text("TAMBAHKAN", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRowDialog = false }) {
                    Text("BATAL", color = Color.White)
                }
            },
            containerColor = DarkGrey,
            titleContentColor = Color.White
        )
    }
}

// Local formatters to avoid import errors
private fun formatRupiahLocal(value: Double): String {
    val formatter = DecimalFormat("#,###,###,###,###")
    return "Rp " + formatter.format(value).replace(",", ".")
}

// Indonesian Terbilang generator spelling engine
fun angkaToTerbilang(nominal: Double): String {
    val angka = nominal.toLong()
    if (angka == 0L) return "Nol Rupiah"
    
    val words = arrayOf(
        "", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"
    )
    
    fun sebut(n: Long): String {
        return when {
            n < 12 -> words[n.toInt()]
            n < 20 -> sebut(n - 10) + " Belas"
            n < 100 -> sebut(n / 10) + " Puluh " + sebut(n % 10)
            n < 200 -> "Seratus " + sebut(n - 100)
            n < 1000 -> sebut(n / 100) + " Ratus " + sebut(n % 100)
            n < 2000 -> "Seribu " + sebut(n - 1000)
            n < 1000000 -> sebut(n / 1000) + " Ribu " + sebut(n % 1000)
            n < 1000000000 -> sebut(n / 1000000) + " Juta " + sebut(n % 1000000)
            n < 1000000000000L -> sebut(n / 1000000000L) + " Milyar " + sebut(n % 1000000000L)
            else -> sebut(n / 1000000000000L) + " Triliun " + sebut(n % 1000000000000L)
        }
    }
    
    val hasil = sebut(angka).replace("\\s+".toRegex(), " ").trim()
    return "$hasil Rupiah"
}
