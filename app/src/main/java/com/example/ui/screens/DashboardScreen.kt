package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    val pendingDeliveries = remember(deliveries) {
        deliveries.filter { it.status == "TERTUNDA" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPure)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header Title block
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSTRUCTION LOGISTICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = LimeNeon,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Dasbor Utama",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = { /* Refresh could go here */ },
                    modifier = Modifier
                        .border(1.dp, BorderGrey, SharpShapes.small)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Summary Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Active Projects
                SummaryCard(
                    title = "TOTAL PROYEK AKTIF",
                    value = "${summary.activeProjects}",
                    subtitle = "Situs konstruksi dalam pengawasan",
                    icon = Icons.Default.Business,
                    valueColor = Color.White,
                    testTag = "summary_active_projects"
                )

                // Pending Deliveries
                SummaryCard(
                    title = "PENGIRIMAN MATERIAL TERTUNDA",
                    value = "${summary.pendingDeliveries}",
                    subtitle = "Surat jalan menunggu konfirmasi lapangan",
                    icon = Icons.Default.LocalShipping,
                    valueColor = if (summary.pendingDeliveries > 0) ColorPending else ColorSuccess,
                    testTag = "summary_pending_deliveries"
                )

                // Sisa Penagihan
                SummaryCard(
                    title = "TOTAL SISA PENAGIHAN",
                    value = formatRupiah(summary.totalSisaPenagihan),
                    subtitle = "Volume material terkirim - sudah terbayar",
                    icon = Icons.Default.AccountBalanceWallet,
                    valueColor = LimeNeon,
                    testTag = "summary_billing_remaining"
                )
            }
        }

        // Weekly Delivery Intensity (Bar Chart)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGrey, SharpShapes.medium),
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                shape = SharpShapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INTENSITAS PENGIRIMAN DATA MINGGUAN (Rp)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Simple pure mathematical custom rendering of a beautiful bar chart
                    val days = listOf("SEN", "SEL", "RAB", "KAM", "JUM", "SAB", "MIN")
                    val maxVal = weeklyIntensity.maxOrNull() ?: 1.0
                    val scaleMax = if (maxVal == 0.0) 1.0 else maxVal
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyIntensity.forEachIndexed { index, value ->
                            val heightRatio = (value / scaleMax).toFloat()
                            val barHeightModifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightRatio.coerceAtLeast(0.04f))
                                .padding(horizontal = 4.dp)
                                .background(if (value > 0) LimeNeon else BorderGrey)

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
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = LimeNeon,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Box(modifier = barHeightModifier)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = days[index],
                                    style = MaterialTheme.typography.labelSmall,
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Penting",
                        tint = ColorPending,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Konfirmasi Pengiriman Masuk",
                        style = MaterialTheme.typography.titleMedium,
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
                        .border(1.dp, BorderGrey, SharpShapes.medium)
                        .testTag("pending_delivery_item_${delivery.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = projectName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${delivery.materialType} | Vol: ${delivery.quantity} ${delivery.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Sopir: ${delivery.driverName} (${delivery.plateNumber})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { viewModel.updateDeliveryStatus(delivery.id, "SUKSES") },
                            colors = ButtonDefaults.buttonColors(containerColor = LimeNeon),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = SharpShapes.small,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("verify_delivery_button_${delivery.id}")
                        ) {
                            Text(
                                text = "TERIMA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGrey, SharpShapes.medium),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                    shape = SharpShapes.medium
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Semua Sukses",
                                tint = ColorSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Semua pengiriman lapangan telah diferivikasi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for BottomNav
        }
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
            .border(1.dp, BorderGrey, SharpShapes.medium)
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
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = valueColor.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, BorderGrey, SharpShapes.small)
                    .padding(8.dp)
            )
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
