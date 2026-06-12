package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LogisticsDelivery
import com.example.ui.components.ManualBookDialog
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val weeklyIntensity by viewModel.weeklyDeliveryIntensityFlag.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val projects by viewModel.projects.collectAsState()

    var showManualDialog by remember { mutableStateOf(false) }

    val pendingDeliveries = remember(deliveries) {
        deliveries.filter { it.status == "TERTUNDA" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPure)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Elegant Header
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SISTEM MONITORING",
                        style = MaterialTheme.typography.labelSmall,
                        color = CorporateBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dasbor Utama",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                IconButton(
                    onClick = { /* Action to refresh */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = DarkGrey,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Minimalist PDF Manual triggers
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManualDialog = true }
                    .testTag("dashboard_manual_book_card"),
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                shape = SharpShapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Buku Panduan",
                        tint = CorporateBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Buku Panduan Operasional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Petunjuk digital penginputan surat jalan, integrasi PPN/DP, dan cetak PDF invoice lunas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Buka",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Clean & compact Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "PROYEK AKTIF",
                    value = "${summary.activeProjects}",
                    subtitle = "Situs konstruksi terdaftar",
                    icon = Icons.Default.Business,
                    valueColor = Color.White,
                    testTag = "summary_active_projects"
                )

                SummaryCard(
                    title = "PENGIRIMAN TERTUNDA",
                    value = "${summary.pendingDeliveries}",
                    subtitle = "Menunggu verifikasi penerimaan",
                    icon = Icons.Default.LocalShipping,
                    valueColor = if (summary.pendingDeliveries > 0) ColorPending else ColorSuccess,
                    testTag = "summary_pending_deliveries"
                )

                SummaryCard(
                    title = "SISA PENAGIHAN KONTRAK",
                    value = formatRupiah(summary.totalSisaPenagihan),
                    subtitle = "Akumulasi nilai material belum tertagih",
                    icon = Icons.Default.AccountBalanceWallet,
                    valueColor = CorporateBlue,
                    testTag = "summary_billing_remaining"
                )
            }
        }

        // Clean Bar Chart with Soft Columns
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                shape = SharpShapes.medium
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "INTENSITAS PENGIRIMAN DATA MINGGUAN (Rp)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val days = listOf("SEN", "SEL", "RAB", "KAM", "JUM", "SAB", "MIN")
                    val maxVal = weeklyIntensity.maxOrNull() ?: 1.0
                    val scaleMax = if (maxVal == 0.0) 1.0 else maxVal
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyIntensity.forEachIndexed { index, value ->
                            val heightRatio = (value / scaleMax).toFloat()

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (value > 0) {
                                    Text(
                                        text = formatShortRupiah(value),
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = CorporateBlue,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(heightRatio.coerceAtLeast(0.06f))
                                        .background(
                                            color = if (value > 0) CorporateBlue else BorderGrey,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = days[index],
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (value > 0) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live list section: Pending Deliveries Alerts
        if (pendingDeliveries.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Penting",
                        tint = ColorPending,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Konfirmasi Pengiriman",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            items(pendingDeliveries) { delivery ->
                val project = projects.find { it.id == delivery.projectId }
                val projectName = project?.name ?: "Proyek Unspecified"
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_delivery_item_${delivery.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = projectName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${delivery.materialType} | Vol: ${delivery.quantity} ${delivery.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Sopir: ${delivery.driverName} (${delivery.plateNumber})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { viewModel.updateDeliveryStatus(delivery.id, "SUKSES") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CorporateBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            shape = SharpShapes.medium,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("verify_delivery_button_${delivery.id}")
                        ) {
                            Text(
                                text = "TERIMA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Semua Sukses",
                                tint = ColorSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Semua pengiriman lapangan selesai terverifikasi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(50.dp)) // Nice footer padding
        }
    }

    if (showManualDialog) {
        ManualBookDialog(onDismiss = { showManualDialog = false })
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = DarkGrey),
        shape = SharpShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MediumGrey, SharpShapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun formatRupiah(value: Double): String {
    val formatter = DecimalFormat("#,###")
    return "Rp " + formatter.format(value).replace(",", ".")
}

fun formatShortRupiah(value: Double): String {
    return when {
        value >= 1_000_000_000 -> {
            val formatted = DecimalFormat("#.#").format(value / 1_000_000_000)
            "${formatted}M"
        }
        value >= 1_000_000 -> {
            val formatted = DecimalFormat("#.#").format(value / 1_000_000)
            "${formatted}Jt"
        }
        else -> {
            DecimalFormat("#,###").format(value).replace(",", ".")
        }
    }
}
