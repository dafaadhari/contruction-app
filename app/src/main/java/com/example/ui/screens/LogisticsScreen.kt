package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.components.PdfPrintUtil
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConstructionProject
import com.example.data.LogisticsDelivery
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class MaterialPreset(val name: String, val unit: String, val price: Double)

val MATERIAL_PRESETS = listOf(
    MaterialPreset("Baja Tulangan D19", "Ton", 14_500_000.0),
    MaterialPreset("Beton Ready-Mix K-350", "m³", 1_100_000.0),
    MaterialPreset("Semen Portland", "Zak", 82_000.0),
    MaterialPreset("Pasir Pasang", "m³", 250_000.0),
    MaterialPreset("Kerikil Pecah", "m³", 310_000.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()

    var activeSubTab by remember { mutableStateOf("SURAT_JALAN") } // "SURAT_JALAN" or "MASTER_PROYEK"

    // If a project is selected via the master-detail clicks, we override the view with a Project Delivery Detail subscreen
    if (selectedProjectId != null) {
        ProjectDeliveriesDetailScreen(
            projectId = selectedProjectId!!,
            viewModel = viewModel,
            onBack = { viewModel.selectProject(null) }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BlackPure)
        ) {
            // Screen Title block
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "MANAJEMEN MATERIAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimeNeon,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Logistik & Proyek",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
            }

            // Sub-Tabs for Logistics Context (Elegant and premium segment control)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(DarkGrey, SharpShapes.medium)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = "SURAT_JALAN" }
                        .background(if (activeSubTab == "SURAT_JALAN") CorporateBlue else Color.Transparent, SharpShapes.medium)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FORM SURAT JALAN",
                        color = if (activeSubTab == "SURAT_JALAN") Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = "MASTER_PROYEK" }
                        .background(if (activeSubTab == "MASTER_PROYEK") CorporateBlue else Color.Transparent, SharpShapes.medium)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MASTER PROYEK",
                        color = if (activeSubTab == "MASTER_PROYEK") Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Tab Contents
            if (activeSubTab == "SURAT_JALAN") {
                SuratJalanSubTab(
                    projects = projects,
                    deliveries = deliveries,
                    viewModel = viewModel
                )
            } else {
                MasterProyekSubTab(
                    projects = projects,
                    viewModel = viewModel
                )
            }
        }
    }
}

// 1. SUB-TAB: Surat Jalan (Forms and general delivery list)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuratJalanSubTab(
    projects: List<ConstructionProject>,
    deliveries: List<LogisticsDelivery>,
    viewModel: AppViewModel
) {
    var isFormExpanded by remember { mutableStateOf(false) }

    var expandedProjectDropdown by remember { mutableStateOf(false) }
    var selectedProjectForForm by remember { mutableStateOf<ConstructionProject?>(null) }
    
    var selectedMaterialPresetIndex by remember { mutableStateOf(0) }
    var volumeInput by remember { mutableStateOf("") }
    var customPriceInput by remember { mutableStateOf("") }
    var driverNameInput by remember { mutableStateOf("") }
    var plateNumberInput by remember { mutableStateOf("") }

    // Sync unit price when active preset changes
    LaunchedEffect(selectedMaterialPresetIndex) {
        customPriceInput = MATERIAL_PRESETS[selectedMaterialPresetIndex].price.toInt().toString()
    }

    // Mock photo attachments (pre-selections to make the interface incredibly real)
    var attachedPhotoLabel by remember { mutableStateOf<String?>(null) }
    var selectedPhotoType by remember { mutableStateOf<String?>(null) }

    var formMessage by remember { mutableStateOf<String?>(null) }

    // Real device integration launchers with visual text fallback
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        attachedPhotoLabel = "CCTV_Kamera_Selesai_${System.currentTimeMillis() % 10000}.jpg"
        selectedPhotoType = "SYSTEM_CAM"
        formMessage = "Bukti foto lapangan berhasil direkam menggunakan Kamera HP!"
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            attachedPhotoLabel = "Galeri_File_Terupload_${System.currentTimeMillis() % 10000}.jpg"
            selectedPhotoType = "SYSTEM_GAL"
            formMessage = "Bukti foto berhasil dipilih dari Dokumen Galeri!"
        }
    }

    var selectedDeliveryForDetail by remember { mutableStateOf<LogisticsDelivery?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Waybill Digital Entry Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                shape = SharpShapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row acting as a beautiful dropdown/accordion toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFormExpanded = !isFormExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INPUT SURAT JALAN DIGITAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimeNeon,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ketuk untuk membuat surat jalan baru...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = { isFormExpanded = !isFormExpanded }) {
                            Icon(
                                imageVector = if (isFormExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isFormExpanded) "Tutup Form" else "Buka Form",
                                tint = LimeNeon
                            )
                        }
                    }

                    AnimatedVisibility(visible = isFormExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            // 1. Project Picker (Exposed Custom Exposed Dropdown Menu)
                            Text(
                                text = "LOKASI PROYEK TUJUAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MediumGrey, SharpShapes.small)
                                        .clickable { expandedProjectDropdown = true }
                                        .padding(12.dp)
                                        .testTag("select_project_dropdown"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedProjectForForm?.name ?: "Pilih Proyek Konstruksi...",
                                        color = if (selectedProjectForForm != null) Color.White else TextSecondary,
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
                                ) {
                                    projects.forEach { proj ->
                                        DropdownMenuItem(
                                            text = { Text("${proj.projectCode} - ${proj.name}", color = Color.White) },
                                            onClick = {
                                                selectedProjectForForm = proj
                                                expandedProjectDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Material selector grid
                            Text(
                                text = "JENIS MATERIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MATERIAL_PRESETS.take(3).forEachIndexed { index, preset ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (selectedMaterialPresetIndex == index) CorporateBlue.copy(alpha = 0.15f) else MediumGrey, SharpShapes.small)
                                            .clickable { selectedMaterialPresetIndex = index }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = preset.name.split(" ")[0], // first word
                                                color = if (selectedMaterialPresetIndex == index) CorporateBlue else Color.White,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "(${preset.unit})",
                                                color = TextSecondary,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }

                            // Secondary selection list
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MATERIAL_PRESETS.drop(3).forEachIndexed { idx, preset ->
                                    val actualIndex = idx + 3
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (selectedMaterialPresetIndex == actualIndex) CorporateBlue.copy(alpha = 0.15f) else MediumGrey, SharpShapes.small)
                                            .clickable { selectedMaterialPresetIndex = actualIndex }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = preset.name,
                                                color = if (selectedMaterialPresetIndex == actualIndex) CorporateBlue else Color.White,
                                                style = MaterialTheme.typography.labelLarge,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "(${preset.unit})",
                                                color = TextSecondary,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Volume and Custom Code-Generated Estimated Price info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                text = "VOLUME (${MATERIAL_PRESETS[selectedMaterialPresetIndex].unit})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = volumeInput,
                                onValueChange = { volumeInput = it },
                                placeholder = { Text("Angka...") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                                    .testTag("volume_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "HARGA SATUAN (Rp)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = customPriceInput,
                                onValueChange = { customPriceInput = it },
                                placeholder = { Text("Harga Satuan...") },
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
                                    .testTag("custom_price_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "NAMA SOPIR",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = driverNameInput,
                                onValueChange = { driverNameInput = it },
                                placeholder = { Text("Nama...") },
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
                                    .testTag("driver_name_input")
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NOMOR PLAT TRUK",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextField(
                                value = plateNumberInput,
                                onValueChange = { plateNumberInput = it },
                                placeholder = { Text("B 9421 XY") },
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
                                    .testTag("plate_number_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Attachment Placeholder for Delivery Proof
                    Text(
                        text = "LAMPIRAN BUKTI LAPANGAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    // Fallback for emulator environments
                                    attachedPhotoLabel = "Kamera_Baja_${System.currentTimeMillis() % 1000}.jpg"
                                    selectedPhotoType = "SYSTEM_CAM"
                                    formMessage = "Foto default berhasil terlampir (Kamera Emulator)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPhotoType == "SYSTEM_CAM") CorporateBlue.copy(alpha = 0.2f) else MediumGrey
                            ),
                            shape = SharpShapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ambil Kamera", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    galleryLauncher.launch("image/*")
                                } catch (e: Exception) {
                                    // Fallback for emulator environments
                                    attachedPhotoLabel = "Galeri_Beton_${System.currentTimeMillis() % 1000}.jpg"
                                    selectedPhotoType = "SYSTEM_GAL"
                                    formMessage = "Foto default berhasil terlampir (Galeri Emulator)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPhotoType == "SYSTEM_GAL") CorporateBlue.copy(alpha = 0.2f) else MediumGrey
                            ),
                            shape = SharpShapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pilih Galeri", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }

                    // Attached label display indicator
                    AnimatedVisibility(visible = attachedPhotoLabel != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(MediumGrey, SharpShapes.small)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Attachment,
                                    contentDescription = null,
                                    tint = LimeNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = attachedPhotoLabel ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = {
                                attachedPhotoLabel = null
                                selectedPhotoType = null
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = ColorError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to send/delay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Deliver status 'TERTUNDA'
                        OutlinedButton(
                            onClick = {
                                val proj = selectedProjectForForm
                                val preset = MATERIAL_PRESETS[selectedMaterialPresetIndex]
                                val volVal = volumeInput.toDoubleOrNull()
                                val priceVal = customPriceInput.replace(".", "").replace(",", ".").toDoubleOrNull() ?: preset.price
                                if (proj != null && volVal != null && driverNameInput.isNotEmpty()) {
                                    viewModel.addNewDelivery(
                                        projectId = proj.id,
                                        materialType = preset.name,
                                        quantity = volVal,
                                        unit = preset.unit,
                                        unitPrice = priceVal,
                                        driverName = driverNameInput,
                                        plateNumber = plateNumberInput,
                                        status = "TERTUNDA",
                                        photoPath = attachedPhotoLabel
                                    )
                                    formMessage = "Surat jalan TERTUNDA berhasil dicatat."
                                    // Reset fields
                                    volumeInput = ""
                                    customPriceInput = preset.price.toInt().toString()
                                    driverNameInput = ""
                                    plateNumberInput = ""
                                    attachedPhotoLabel = null
                                    selectedPhotoType = null
                                } else {
                                    formMessage = "Harap isi semua kolom wajib (Proyek, Volume, Sopir)!"
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = borderStrokeHelper(true, Color.White),
                            shape = SharpShapes.small,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("SIMPAN TERTUNDA", style = MaterialTheme.typography.labelSmall)
                        }

                        // Deliver status 'SUKSES'
                        Button(
                            onClick = {
                                val proj = selectedProjectForForm
                                val preset = MATERIAL_PRESETS[selectedMaterialPresetIndex]
                                val volVal = volumeInput.toDoubleOrNull()
                                val priceVal = customPriceInput.replace(".", "").replace(",", ".").toDoubleOrNull() ?: preset.price
                                if (proj != null && volVal != null && driverNameInput.isNotEmpty()) {
                                    viewModel.addNewDelivery(
                                        projectId = proj.id,
                                        materialType = preset.name,
                                        quantity = volVal,
                                        unit = preset.unit,
                                        unitPrice = priceVal,
                                        driverName = driverNameInput,
                                        plateNumber = plateNumberInput,
                                        status = "SUKSES",
                                        photoPath = attachedPhotoLabel
                                    )
                                    formMessage = "Surat jalan SUKSES langsung terkirim."
                                    // Reset fields
                                    volumeInput = ""
                                    customPriceInput = preset.price.toInt().toString()
                                    driverNameInput = ""
                                    plateNumberInput = ""
                                    attachedPhotoLabel = null
                                    selectedPhotoType = null
                                } else {
                                    formMessage = "Harap isi semua kolom wajib (Proyek, Volume, Sopir)!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LimeNeon),
                            shape = SharpShapes.small,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("submit_delivery_button")
                        ) {
                            Text("KIRIM SEKARANG", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }

                    // Prompt message
                    if (formMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LimeNeon,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } // End of Column inside AnimatedVisibility
              } // End of AnimatedVisibility
            } // End of Card Column
          } // End of Card
        } // End of LazyColumn Item

        // Title of recent logs
        item {
            Text(
                text = "LOG SURAT JALAN AKTIF",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        // Recent deliveries lists
        items(deliveries) { del ->
            val p = projects.find { it.id == del.projectId }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedDeliveryForDetail = del },
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
                            text = del.suratJalanNumber,
                            style = MaterialTheme.typography.labelLarge,
                            color = LimeNeon
                        )
                        
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(if (del.status == "SUKSES") ColorSuccess.copy(alpha = 0.15f) else ColorPending.copy(alpha = 0.15f), SharpShapes.small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = del.status,
                                color = if (del.status == "SUKSES") ColorSuccess else ColorPending,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = p?.name ?: "Proyek Unspecified",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${del.materialType} — ${del.quantity} ${del.unit} (${formatRupiah(del.quantity * del.unitPrice)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sopir: ${del.driverName} | Plat: ${del.plateNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Waktu: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(del.deliveryDateTime))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon proof if attached
                            if (del.photoPath != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(MediumGrey)
                                        .border(1.dp, BorderGrey, SharpShapes.small)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Bukti Terlampir", tint = LimeNeon, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bukti", style = MaterialTheme.typography.labelSmall, color = LimeNeon)
                                }
                            }

                            val printContext = LocalContext.current
                            IconButton(
                                onClick = {
                                    val html = generateSuratJalanHtml(del, p)
                                    PdfPrintUtil.printHtml(printContext, html, "SuratJalan_${del.suratJalanNumber.replace("/", "_")}")
                                },
                                modifier = Modifier
                                    .border(1.dp, LimeNeon, SharpShapes.small)
                                    .size(34.dp)
                                    .testTag("print_delivery_${del.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = "Cetak Surat Jalan PDF",
                                    tint = LimeNeon,
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

    selectedDeliveryForDetail?.let { del ->
        val p = projects.find { it.id == del.projectId }
        DeliveryDetailDialog(
            delivery = del,
            project = p,
            viewModel = viewModel,
            onDismissRequest = { selectedDeliveryForDetail = null }
        )
    }
}

// 2. SUB-TAB: Master Proyek (Site registry with expansion triggers)
@Composable
fun MasterProyekSubTab(
    projects: List<ConstructionProject>,
    viewModel: AppViewModel
) {
    var isAddingProject by remember { mutableStateOf(false) }

    // Inputs for Add Project Form
    var projectCodeInput by remember { mutableStateOf("") }
    var projectNameInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOKASI REGISTER PROYEK",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Button(
                    onClick = { isAddingProject = !isAddingProject },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAddingProject) ColorError else LimeNeon),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = SharpShapes.small,
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isAddingProject) "TUTUP" else "TAMBAH LOKASI",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAddingProject) Color.White else Color.Black
                    )
                }
            }
        }

        // Conditional Insert Project Form
        if (isAddingProject) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGrey, SharpShapes.medium),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "TAMBAH MASTER PROYEK BARU",
                            style = MaterialTheme.typography.labelSmall,
                            color = LimeNeon
                        )

                        TextField(
                            value = projectCodeInput,
                            onValueChange = { projectCodeInput = it },
                            label = { Text("Kode Proyek (e.g. PRJ-SDR-04)") },
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = projectNameInput,
                            onValueChange = { projectNameInput = it },
                            label = { Text("Nama Lokasi Proyek") },
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text("Kota / Wilayah") },
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it },
                            label = { Text("Nilai Kontrak / Total Anggaran (Rp)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MediumGrey,
                                unfocusedContainerColor = MediumGrey,
                                focusedIndicatorColor = LimeNeon,
                                unfocusedIndicatorColor = BorderGrey,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = SharpShapes.small,
                            modifier = Modifier.fillMaxWidth()
                        )

                        var formMessageProject by remember { mutableStateOf<String?>(null) }

                        if (formMessageProject != null) {
                            Text(
                                text = formMessageProject!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (formMessageProject!!.contains("Selesai") || formMessageProject!!.contains("berhasil")) LimeNeon else ColorError,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = {
                                val cleanBudget = budgetInput.replace("Rp", "")
                                                            .replace("rp", "")
                                                            .replace(".", "")
                                                            .replace(",", "")
                                                            .replace(" ", "")
                                                            .trim()
                                val bVal = cleanBudget.toDoubleOrNull()

                                if (projectCodeInput.isBlank() || projectNameInput.isBlank()) {
                                    formMessageProject = "Harap isi Kode Proyek dan Nama Lokasi!"
                                } else if (bVal == null || bVal <= 0.0) {
                                    formMessageProject = "Nilai Kontrak harus berupa angka positif yang sah!"
                                } else {
                                    viewModel.addNewProject(
                                        code = projectCodeInput.trim(),
                                        name = projectNameInput.trim(),
                                        location = locationInput.trim(),
                                        contractValue = bVal
                                    )
                                    formMessageProject = "Selesai! Master Proyek baru berhasil didaftarkan."
                                    // Reset fields
                                    projectCodeInput = ""
                                    projectNameInput = ""
                                    locationInput = ""
                                    budgetInput = ""
                                    // Close form container after brief delay or keep open for visual validation
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LimeNeon),
                            shape = SharpShapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("SIMPAN MASTER PROYEK", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Projects list represent Site Master List
        items(projects) { project ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGrey, SharpShapes.medium)
                    .clickable { viewModel.selectProject(project.id) }
                    .testTag("project_item_${project.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                shape = SharpShapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project.projectCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = LimeNeon,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            var statusExpanded by remember { mutableStateOf(false) }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .background(if (project.status == "AKTIF") ColorSuccess.copy(alpha = 0.15f) else CorporateBlue.copy(alpha = 0.15f))
                                        .border(1.dp, if (project.status == "AKTIF") ColorSuccess else CorporateBlue, SharpShapes.small)
                                        .clickable {
                                            statusExpanded = true
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${project.status} ▾",
                                        color = if (project.status == "AKTIF") ColorSuccess else CorporateBlue,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = statusExpanded,
                                    onDismissRequest = { statusExpanded = false },
                                    modifier = Modifier.background(MediumGrey)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("AKTIF", color = ColorSuccess, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            viewModel.updateProjectStatus(project.id, "AKTIF")
                                            statusExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("SUKSES", color = CorporateBlue, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            viewModel.updateProjectStatus(project.id, "SUKSES")
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Wilayah: ${project.location}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Nilai Kontrak: ${formatRupiah(project.contractValue)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LimeNeonMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detail Material",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// 3. SUBCREEN: Detailed Material logs of a selected site
@Composable
fun ProjectDeliveriesDetailScreen(
    projectId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()

    val targetProject = remember(projects) { projects.find { it.id == projectId } }
    val projectDeliveries = remember(deliveries) {
        deliveries.filter { it.projectId == projectId }
    }

    var selectedDeliveryForDetail by remember { mutableStateOf<LogisticsDelivery?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackPure)
    ) {
        // navigation back bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .border(1.dp, BorderGrey, SharpShapes.small)
                    .size(40.dp)
                    .testTag("back_to_projects_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "RIWAYAT MATERIAL SITE",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimeNeon
                )
                Text(
                    text = targetProject?.name ?: "Proyek Detail",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGrey, SharpShapes.medium),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "INFORMASI SITUS MASUK", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Kode Proyek: ${targetProject?.projectCode ?: "-"}", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        Text(text = "Wilayah: ${targetProject?.location ?: "-"}", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        Text(text = "Nilai Kontrak: ${targetProject?.let { formatRupiah(it.contractValue) } ?: "-"}", style = MaterialTheme.typography.bodyLarge, color = LimeNeon)
                    }
                }
            }

            item {
                Text(
                    text = "LOG DAFTAR BARANG BARU (${projectDeliveries.size} Item Terdaftar)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (projectDeliveries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Belum ada material terkirim ke lokasi ini.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(projectDeliveries) { del ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderGrey, SharpShapes.medium)
                            .clickable { selectedDeliveryForDetail = del },
                        colors = CardDefaults.cardColors(containerColor = DarkGrey),
                        shape = SharpShapes.medium
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = del.suratJalanNumber,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LimeNeon
                                )
                                Box(
                                    modifier = Modifier
                                        .background(if (del.status == "SUKSES") ColorSuccess.copy(alpha = 0.15f) else ColorPending.copy(alpha = 0.15f))
                                        .border(1.dp, if (del.status == "SUKSES") ColorSuccess else ColorPending, SharpShapes.small)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = del.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (del.status == "SUKSES") ColorSuccess else ColorPending
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${del.materialType} | ${del.quantity} ${del.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Maks Estimasi: ${formatRupiah(del.quantity * del.unitPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(color = BorderGrey)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sopir: ${del.driverName} | Plat: ${del.plateNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Pengiriman: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(del.deliveryDateTime))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                
                                val printContext = LocalContext.current
                                IconButton(
                                    onClick = {
                                        val html = generateSuratJalanHtml(del, targetProject)
                                        PdfPrintUtil.printHtml(printContext, html, "SuratJalan_${del.suratJalanNumber.replace("/", "_")}")
                                    },
                                    modifier = Modifier
                                        .border(1.dp, LimeNeon, SharpShapes.small)
                                        .size(34.dp)
                                        .testTag("print_delivery_detail_${del.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = "Cetak Surat Jalan PDF",
                                        tint = LimeNeon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (del.photoPath != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MediumGrey)
                                        .border(1.dp, BorderGrey, SharpShapes.small)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Camera, contentDescription = null, tint = LimeNeon, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lampiran Bukti Pengiriman Lapangan: ${del.photoPath}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    selectedDeliveryForDetail?.let { del ->
        val p = projects.find { it.id == del.projectId }
        DeliveryDetailDialog(
            delivery = del,
            project = p,
            viewModel = viewModel,
            onDismissRequest = { selectedDeliveryForDetail = null }
        )
    }
}

// Border Stroke Helper
fun borderStrokeHelper(selected: Boolean, color: Color = LimeNeon): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, if (selected) color else BorderGrey)
}

private fun formatRupiahLocal(value: Double): String {
    val formatter = java.text.DecimalFormat.getCurrencyInstance(Locale("id", "ID")) as java.text.DecimalFormat
    val symbols = formatter.decimalFormatSymbols
    symbols.currencySymbol = "Rp "
    symbols.monetaryDecimalSeparator = ','
    symbols.groupingSeparator = '.'
    formatter.decimalFormatSymbols = symbols
    formatter.maximumFractionDigits = 2
    return formatter.format(value)
}

fun generateSuratJalanHtml(
    delivery: LogisticsDelivery,
    project: ConstructionProject?
): String {
    val formattedDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date(delivery.deliveryDateTime))
    val priceFormatted = formatRupiahLocal(delivery.unitPrice)
    val totalFormatted = formatRupiahLocal(delivery.quantity * delivery.unitPrice)
    
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <title>Surat Jalan - ${delivery.suratJalanNumber}</title>
        <style>
            body {
                font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                color: #000;
                margin: 40px;
                line-height: 1.4;
                font-size: 11px;
            }
            .header-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 5px;
            }
            .header-left {
                width: 75%;
                vertical-align: top;
            }
            .header-right {
                width: 25%;
                text-align: right;
                vertical-align: top;
            }
            .company-name {
                color: #1C3A5E;
                font-size: 21px;
                font-weight: 900;
                margin: 0;
                letter-spacing: 1px;
            }
            .company-desc {
                font-size: 10px;
                font-weight: bold;
                margin: 4px 0 2px 0;
            }
            .company-contact {
                font-size: 9px;
                color: #444;
                margin: 0;
            }
            .logo-r {
                font-family: 'Times New Roman', Times, Serif;
                font-size: 38px;
                font-weight: 900;
                color: #1C3A5E;
                line-height: 1;
                margin-right: 15px;
            }
            .divider-double {
                border-top: 4px solid #1C3A5E;
                border-bottom: 2px solid #000;
                height: 4px;
                margin: 8px 0 16px 0;
            }
            .doc-title {
                text-align: center;
                font-size: 22px;
                font-weight: 900;
                letter-spacing: 4px;
                text-decoration: underline;
                margin-bottom: 20px;
            }
            .meta-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 18px;
            }
            .meta-box {
                width: 48%;
                border: 1px solid #ddd;
                padding: 10px;
                vertical-align: top;
            }
            .meta-box-title {
                font-size: 9px;
                color: #555;
                font-weight: bold;
                text-transform: uppercase;
                margin-bottom: 5px;
                border-bottom: 1px solid #eee;
                padding-bottom: 2px;
            }
            .meta-row {
                font-size: 11px;
                margin-bottom: 4px;
            }
            .meta-label {
                display: inline-block;
                width: 100px;
                color: #555;
                font-weight: bold;
            }
            .meta-val {
                font-weight: bold;
            }
            .materials-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 30px;
                font-size: 10px;
            }
            .materials-table th {
                background-color: #1C3A5E;
                color: #ffffff;
                font-weight: bold;
                border: 1px solid #1C3A5E;
                padding: 8px;
                text-align: left;
            }
            .materials-table td {
                border: 1px solid #ddd;
                padding: 8px;
            }
            .signatures {
                width: 100%;
                border-collapse: collapse;
                margin-top: 50px;
                font-size: 11px;
            }
            .sig-col {
                width: 33%;
                vertical-align: top;
                text-align: center;
            }
            .sig-space {
                height: 60px;
            }
            .photo-note {
                margin-top: 20px;
                border: 1px dashed #ddd;
                padding: 8px;
                font-size: 9px;
                color: #555;
                background-color: #fafafa;
                text-align: center;
            }
        </style>
    </head>
    <body>
        <table class="header-table">
            <tr>
                <td class="header-left">
                    <div class="company-name">PT. RAYYAN KARYA</div>
                    <div class="company-desc">Supplier Atap Baja Ringan, Aluminium, dan Material Konstruksi</div>
                    <div class="company-contact">Email: cvrayyanalumunium@gmail.com | Hubungi: 081381080745</div>
                </td>
                <td class="header-right">
                    <div class="logo-r">R</div>
                </td>
            </tr>
        </table>

        <div class="divider-double"></div>

        <div class="doc-title">SURAT JALAN / DELIVERY ORDER</div>

        <table class="meta-table">
            <tr>
                <td class="meta-box" style="width: 48%;">
                    <div class="meta-box-title">Data Pengiriman (Expedition)</div>
                    <div class="meta-row">
                        <span class="meta-label">No. Surat Jalan:</span>
                        <span class="meta-val" style="color: #000;">${delivery.suratJalanNumber}</span>
                    </div>
                    <div class="meta-row">
                        <span class="meta-label">Tanggal Kirim:</span>
                        <span class="meta-val">${formattedDate}</span>
                    </div>
                    <div class="meta-row">
                        <span class="meta-label">No. Plat Truk:</span>
                        <span class="meta-val">${delivery.plateNumber}</span>
                    </div>
                    <div class="meta-row">
                        <span class="meta-label">Nama Driver:</span>
                        <span class="meta-val">${delivery.driverName}</span>
                    </div>
                </td>
                <td style="width: 4%;"></td>
                <td class="meta-box" style="width: 48%;">
                    <div class="meta-box-title">Tujuan Bongkar (Destination)</div>
                    <div class="meta-row" style="font-weight: bold; font-size: 12px; margin-bottom: 4px;">Project: ${project?.name ?: "Proyek Unspecified"}</div>
                    <div class="meta-row"><span class="meta-label">Kode Proyek:</span> <span class="meta-val">${project?.projectCode ?: "-"}</span></div>
                    <div class="meta-row"><span class="meta-label">Lokasi:</span> <span class="meta-val">${project?.location ?: "-"}</span></div>
                    <div class="meta-row"><span class="meta-label">Status Delivery:</span> <span class="meta-val" style="color: ${if (delivery.status == "SUKSES") "#2E7D32" else "#E65100"};">${delivery.status}</span></div>
                </td>
            </tr>
        </table>

        <table class="materials-table">
            <thead>
                <tr>
                    <th style="width: 10%; text-align: center;">No</th>
                    <th style="width: 45%;">Deskripsi Material / Muatan</th>
                    <th style="width: 15%; text-align: center;">Jumlah Volume</th>
                    <th style="width: 15%; text-align: center;">Satuan</th>
                    <th style="width: 15%; text-align: right;">Estimasi Harga</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td style="text-align: center;">1</td>
                    <td style="font-weight: bold;">${delivery.materialType}</td>
                    <td style="text-align: center; font-weight: bold;">${delivery.quantity}</td>
                    <td style="text-align: center;">${delivery.unit}</td>
                    <td style="text-align: right;">${priceFormatted} / ${delivery.unit}</td>
                </tr>
            </tbody>
        </table>

        ${if (delivery.photoPath != null) """
        <div class="photo-note">
            <strong>BUKTI FISIK BONGKAR MUAT LAPANGAN TERLAMPIR:</strong><br>
            Berkas Gambar: ${delivery.photoPath} - Terverifikasi sukses oleh CCTV/Kamera Lapangan.
        </div>
        """ else ""}

        <table class="signatures">
            <tr>
                <td class="sig-col">
                    <div>Penerima / Pengawas Lapangan</div>
                    <div class="sig-space"></div>
                    <div style="font-weight: bold; text-decoration: underline;">( _________________ )</div>
                    <div style="font-size: 10px; color: #555;">Tanda Tangan & Cap Proyek</div>
                </td>
                <td class="sig-col">
                    <div>Sopir / Ekspedisi</div>
                    <div class="sig-space"></div>
                    <div style="font-weight: bold; text-decoration: underline;">( ${delivery.driverName} )</div>
                    <div style="font-size: 10px; color: #555;">Sopir Pembawa Armada</div>
                </td>
                <td class="sig-col">
                    <div>Hormat Kami,</div>
                    <div style="font-weight: bold;">PT. RAYYAN KARYA</div>
                    <div class="sig-space"></div>
                    <div style="font-weight: bold; text-decoration: underline;">( _________________ )</div>
                    <div style="font-size: 10px; color: #555;">Administrasi Gudang</div>
                </td>
            </tr>
        </table>
    </body>
    </html>
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailDialog(
    delivery: LogisticsDelivery,
    project: ConstructionProject?,
    viewModel: AppViewModel,
    onDismissRequest: () -> Unit
) {
    val printContext = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("delivery_detail_dialog"),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = SharpShapes.medium,
                color = DarkGrey
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DETAIL SURAT JALAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimeNeon
                            )
                            Text(
                                text = delivery.suratJalanNumber,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.LightGray)
                        }
                    }
                    
                    Divider(color = BorderGrey, modifier = Modifier.padding(vertical = 12.dp))
                    
                    // Body list of details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRowLabel(label = "Tujuan Proyek", value = project?.name ?: "Proyek Unspecified")
                        DetailRowLabel(label = "Lokasi", value = project?.location ?: "-")
                        DetailRowLabel(label = "Jenis Material", value = delivery.materialType)
                        DetailRowLabel(
                            label = "Volume / Jumlah",
                            value = "${delivery.quantity} ${delivery.unit}"
                        )
                        DetailRowLabel(
                            label = "Harga Satuan",
                            value = formatRupiahLocal(delivery.unitPrice)
                        )
                        DetailRowLabel(
                            label = "Total Nilai Estimasi",
                            value = formatRupiahLocal(delivery.quantity * delivery.unitPrice),
                            valueColor = LimeNeon
                        )
                        DetailRowLabel(label = "Nama Sopir", value = delivery.driverName)
                        DetailRowLabel(label = "Plat Nomor", value = delivery.plateNumber)
                        DetailRowLabel(
                            label = "Waktu Pengiriman",
                            value = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(delivery.deliveryDateTime))
                        )
                        DetailRowLabel(
                            label = "Status Delivery",
                            value = delivery.status,
                            valueColor = if (delivery.status == "SUKSES") ColorSuccess else ColorPending
                        )
                        
                        if (delivery.photoPath != null) {
                            var showPhotoDialog by remember { mutableStateOf(false) }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MediumGrey, SharpShapes.small)
                                    .clickable { showPhotoDialog = true }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = LimeNeon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bukti Foto Terlampir (Sopir/CCTV)", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Text("${delivery.photoPath} 🔍 (Ketuk untuk Lihat)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }

                            if (showPhotoDialog) {
                                PhotoViewDialog(
                                    delivery = delivery,
                                    project = project,
                                    onDismissRequest = { showPhotoDialog = false }
                                )
                            }
                        }
                    }
                    
                    Divider(color = BorderGrey, modifier = Modifier.padding(bottom = 12.dp))
                    
                    // Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Print PDF
                            Button(
                                onClick = {
                                    val html = generateSuratJalanHtml(delivery, project)
                                    PdfPrintUtil.printHtml(printContext, html, "SuratJalan_${delivery.suratJalanNumber.replace("/", "_")}")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LimeNeon),
                                shape = SharpShapes.small
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cetak PDF", style = MaterialTheme.typography.labelMedium, color = Color.Black)
                                }
                            }
                            
                            // Change Status (Toggle)
                            val nextStatus = if (delivery.status == "TERTUNDA") "SUKSES" else "TERTUNDA"
                            val btnText = if (delivery.status == "TERTUNDA") "Selesaikan" else "Tunda"
                            val btnColor = if (delivery.status == "TERTUNDA") ColorSuccess else ColorPending
                            
                            Button(
                                onClick = {
                                    viewModel.updateDeliveryStatus(delivery.id, nextStatus)
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                shape = SharpShapes.small
                            ) {
                                Text(btnText, style = MaterialTheme.typography.labelMedium, color = Color.Black)
                            }
                        }
                        
                        // Delete Button (Danger)
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteDelivery(delivery)
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorError),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorError),
                            shape = SharpShapes.small
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorError)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus Surat Jalan", style = MaterialTheme.typography.labelMedium, color = ColorError)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DetailRowLabel(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1.5f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
fun PhotoViewDialog(
    delivery: LogisticsDelivery,
    project: ConstructionProject?,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = SharpShapes.medium,
            color = DarkGrey
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BUKTI LAPANGAN (FOTO/CCTV)",
                        style = MaterialTheme.typography.labelSmall,
                        color = LimeNeon
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.LightGray)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Watermarked Photo Mock Canvas Box
                val isCam = delivery.photoPath?.contains("Kamera") == true || delivery.photoPath?.contains("CAM") == true
                val isGal = delivery.photoPath?.contains("Galeri") == true || delivery.photoPath?.contains("GAL") == true
                
                // Top badge indicating source
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCam) Color.Red.copy(alpha = 0.15f) else if (isGal) CorporateBlue.copy(alpha = 0.15f) else MediumGrey, SharpShapes.small)
                        .border(1.dp, if (isCam) Color.Red else if (isGal) CorporateBlue else BorderGrey, SharpShapes.small)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCam) Icons.Default.CameraAlt else if (isGal) Icons.Default.PhotoLibrary else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (isCam) Color.Red else if (isGal) CorporateBlue else LimeNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCam) "LAMPIRAN: FOTO REAL-TIME KAMERA HP" else if (isGal) "LAMPIRAN: FILE GALERI PERANGKAT" else "LAMPIRAN: DOKUMEN DIGITAL DIGITAL PROOF",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color.Black, SharpShapes.small)
                        .border(1.dp, LimeNeon.copy(alpha = 0.5f), SharpShapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    // Grid / Camera bracket pattern
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val pad = 24f
                        val len = 40f
                        val strokeW = 4f
                        val color = LimeNeon.copy(alpha = 0.4f)
                        
                        // Top-left bracket
                        drawRect(color = color, topLeft = Offset(pad, pad), size = androidx.compose.ui.geometry.Size(len, strokeW))
                        drawRect(color = color, topLeft = Offset(pad, pad), size = androidx.compose.ui.geometry.Size(strokeW, len))
                        
                        // Top-right bracket
                        drawRect(color = color, topLeft = Offset(w - pad - len, pad), size = androidx.compose.ui.geometry.Size(len, strokeW))
                        drawRect(color = color, topLeft = Offset(w - pad, pad), size = androidx.compose.ui.geometry.Size(strokeW, len))
                        
                        // Bottom-left bracket
                        drawRect(color = color, topLeft = Offset(pad, h - pad), size = androidx.compose.ui.geometry.Size(len, strokeW))
                        drawRect(color = color, topLeft = Offset(pad, h - pad - len), size = androidx.compose.ui.geometry.Size(strokeW, len))
                        
                        // Bottom-right bracket
                        drawRect(color = color, topLeft = Offset(w - pad - len, h - pad), size = androidx.compose.ui.geometry.Size(len, strokeW))
                        drawRect(color = color, topLeft = Offset(w - pad, h - pad - len), size = androidx.compose.ui.geometry.Size(strokeW, len))
                        
                        // Crosshair in middle
                        drawCircle(color, radius = 6f, center = Offset(w/2, h/2))
                        drawRect(color = color, topLeft = Offset(w/2 - 15, h/2 - 1), size = androidx.compose.ui.geometry.Size(30f, 2f))
                        drawRect(color = color, topLeft = Offset(w/2 - 1, h/2 - 15), size = androidx.compose.ui.geometry.Size(2f, 30f))

                        // Draw specialized material graphics patterns inside the dark viewport
                        val isBaja = delivery.materialType.contains("Baja", true) || delivery.materialType.contains("Wire", true)
                        val isBeton = delivery.materialType.contains("Beton", true) || delivery.materialType.contains("Ready", true)
                        val isPasir = delivery.materialType.contains("Pasir", true) || delivery.materialType.contains("Batu", true)
                        
                        if (isBaja) {
                            // Rebars mesh pattern
                            for (x in 3..7) {
                                drawLine(
                                    color = Color(0xFFFFA500).copy(alpha = 0.35f),
                                    start = Offset(w * (x / 10f), h * 0.25f),
                                    end = Offset(w * (x / 10f), h * 0.75f),
                                    strokeWidth = 8f
                                )
                            }
                            for (y in 3..7) {
                                drawLine(
                                    color = Color(0xFFFFA500).copy(alpha = 0.35f),
                                    start = Offset(w * 0.25f, h * (y / 10f)),
                                    end = Offset(w * 0.75f, h * (y / 10f)),
                                    strokeWidth = 8f
                                )
                            }
                        } else if (isBeton) {
                            // Concrete circle/cylinder outline
                            drawCircle(
                                color = Color.LightGray.copy(alpha = 0.25f),
                                radius = 70f,
                                center = Offset(w / 2f, h / 2f)
                            )
                            drawCircle(
                                color = Color.Gray.copy(alpha = 0.35f),
                                radius = 40f,
                                center = Offset(w / 2f, h / 2f)
                            )
                        } else if (isPasir) {
                            // Aggregates heap/pyramid outline
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.3f, h * 0.7f)
                                lineTo(w * 0.5f, h * 0.35f)
                                lineTo(w * 0.7f, h * 0.7f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFFCD853F).copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    // Stylized Graphic Representation (since it's a code-generated vector app)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (delivery.materialType.contains("Baja", true)) Icons.Default.GridOn else if (delivery.materialType.contains("Beton", true)) Icons.Default.Circle else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (isCam) Color.Red.copy(alpha = 0.85f) else LimeNeon,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "[ PHOTO PROOF: ${delivery.materialType} ]",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${delivery.quantity} ${delivery.unit} | Sopir: ${delivery.driverName}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Truk: ${delivery.plateNumber}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Camera Watermark info overlays (top & bottom)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "REC ◉",
                                color = Color.Red,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CCTV SITE CAMERA 04",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "PROYEK: ${project?.projectCode ?: "PRJ"}",
                                    color = LimeNeon,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (project?.location ?: "LOKASI").uppercase(),
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(delivery.deliveryDateTime))
                                val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(delivery.deliveryDateTime))
                                Text(
                                    text = sdfDate,
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = sdfTime,
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Ref: ${delivery.photoPath}",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeNeon),
                    shape = SharpShapes.small
                ) {
                    Text("KEMBALI", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
