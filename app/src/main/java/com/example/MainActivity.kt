package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogisticsScreen
import com.example.ui.theme.BlackPure
import com.example.ui.theme.BorderGrey
import com.example.ui.theme.DarkGrey
import com.example.ui.components.RayyanKaryaLogo
import com.example.ui.theme.CorporateBlue
import com.example.ui.theme.LimeNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SharpShapes
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val app = context.applicationContext as Application
                val viewModel: AppViewModel = viewModel(
                    factory = AppViewModelFactory(app)
                )

                val currentTab by viewModel.currentTab.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlackPure),
                    topBar = {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RayyanKaryaLogo()
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkGrey),
                                    shape = SharpShapes.small,
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "PORTAL UTAMA",
                                            color = CorporateBlue,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = BorderGrey, thickness = 1.dp)
                        }
                    },
                    bottomBar = {
                        Column {
                            HorizontalDivider(color = BorderGrey, thickness = 1.dp)
                            // Dynamic Bottom Navigation Bar centered on modern Material 3 specifications
                            NavigationBar(
                                modifier = Modifier.testTag("main_bottom_nav"),
                                containerColor = BlackPure,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == "DASHBOARD",
                                    onClick = { viewModel.selectTab("DASHBOARD") },
                                    label = { Text("Dasbor") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Dashboard,
                                            contentDescription = "Dasbor"
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = LimeNeon,
                                        unselectedIconColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                        indicatorColor = LimeNeon
                                    ),
                                    modifier = Modifier.testTag("tab_dashboard")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "LOGISTICS",
                                    onClick = { viewModel.selectTab("LOGISTICS") },
                                    label = { Text("Logistik") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = "Logistik"
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = LimeNeon,
                                        unselectedIconColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                        indicatorColor = LimeNeon
                                    ),
                                    modifier = Modifier.testTag("tab_logistics")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "BILLING",
                                    onClick = { viewModel.selectTab("BILLING") },
                                    label = { Text("Penagihan") },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = "Penagihan"
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = LimeNeon,
                                        unselectedIconColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                        indicatorColor = LimeNeon
                                    ),
                                    modifier = Modifier.testTag("tab_billing")
                                )
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BlackPure)
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            "DASHBOARD" -> DashboardScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            "LOGISTICS" -> LogisticsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            "BILLING" -> BillingScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
