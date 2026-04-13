package com.example.testing

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testing.data.ThemePreference
import com.example.testing.data.local.DatabaseProvider
import com.example.testing.ui.screens.*
import com.example.testing.ui.theme.TestingTheme
import com.example.testing.ui.viewmodel.*
import com.example.testing.utils.AppViewModelFactory
import com.example.testing.utils.PrepopulateData
import com.example.testing.utils.ReminderUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WelcomeDialog(
    onSave: (Double?) -> Unit
) {
    var balance by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { }, // Force decision
        title = { 
            Text(
                "Welcome 💸", 
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold 
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Ready to master your finances?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Let's start by setting up your primary cash balance. You can add more wallets for bank accounts or credit cards later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = balance,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) balance = it },
                    label = { Text("Initial Cash Balance") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "This creates your 'Cash' wallet automatically.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = balance.toDoubleOrNull() ?: 0.0
                    onSave(amount)
                },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Get Started", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onSave(0.0) }) {
                Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object TransactionList : Screen("transaction_list")
    object AddTransaction : Screen("add_transaction")
    object Wallets : Screen("wallets")
    object Analysis : Screen("analysis")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeState by themeViewModel.themeState.collectAsStateWithLifecycle()

            val isDarkTheme = when (val state = themeState) {
                is ThemeLoadState.Loading -> androidx.compose.foundation.isSystemInDarkTheme()
                is ThemeLoadState.Ready -> state.isDark ?: androidx.compose.foundation.isSystemInDarkTheme()
            }

            TestingTheme(darkTheme = isDarkTheme) {
                val context = LocalContext.current
                val db = DatabaseProvider.getDatabase(applicationContext)
                
                val isFirstLaunch by ThemePreference.isFirstLaunch(context).collectAsState(initial = false)
                var showWelcome by remember { mutableStateOf(false) }

                LaunchedEffect(isFirstLaunch) {
                    if (isFirstLaunch) {
                        showWelcome = true
                    }
                }

                // Notification Permission Request
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    } else {
                        mutableStateOf(true)
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission = isGranted
                        if (isGranted) {
                            ReminderUtils.scheduleReminder(context)
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    PrepopulateData.insertDefaults(db)
                    createNotificationChannel(applicationContext)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            ReminderUtils.scheduleReminder(applicationContext)
                        }
                    } else {
                        ReminderUtils.scheduleReminder(applicationContext)
                    }
                }

                val factory = AppViewModelFactory(db)

                val txViewModel: TransactionViewModel = viewModel(factory = factory)
                val walletViewModel: WalletViewModel = viewModel(factory = factory)
                val categoryViewModel: CategoryViewModel = viewModel(factory = factory)
                val personViewModel: PersonViewModel = viewModel(factory = factory)
                val tagViewModel: TagViewModel = viewModel(factory = factory)

                if (showWelcome) {
                    WelcomeDialog(
                        onSave = { amount: Double? ->
                            CoroutineScope(Dispatchers.IO).launch {
                                amount?.let {
                                    walletViewModel.setCashOpeningBalance(it)
                                }
                                ThemePreference.setFirstLaunchDone(context)
                                showWelcome = false
                            }
                        }
                    )
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            val items = listOf(
                                Triple(Screen.Dashboard.route, "Home", Icons.Default.Home),
                                Triple(Screen.TransactionList.route, "History", Icons.AutoMirrored.Filled.List),
                                Triple(Screen.Analysis.route, "Analysis", Icons.Default.BarChart),
                                Triple(Screen.Wallets.route, "Wallets", Icons.Default.AccountBalanceWallet)
                            )
                            items.forEach { (route, label, icon) ->
                                val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                                NavigationBarItem(
                                    icon = { 
                                        Icon(
                                            icon, 
                                            contentDescription = label,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ) 
                                    },
                                    label = { 
                                        Text(
                                            label,
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                            style = MaterialTheme.typography.labelMedium
                                        ) 
                                    },
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        val route = navBackStackEntry?.destination?.route
                        if (route != Screen.AddTransaction.route) {
                            FloatingActionButton(
                                onClick = { navController.navigate(Screen.AddTransaction.route) },
                                containerColor = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp),
                                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = txViewModel,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = {
                                    themeViewModel.toggleTheme(isDarkTheme)
                                },
                                onViewTransactionsClick = {
                                    navController.navigate(Screen.TransactionList.route)
                                },
                                onViewWalletsClick = {
                                    navController.navigate(Screen.Wallets.route)
                                },
                                onAddTransactionClick = {
                                    navController.navigate(Screen.AddTransaction.route)
                                }
                            )
                        }
                        composable(Screen.Analysis.route) {
                            AnalysisScreen(
                                viewModel = txViewModel,
                                categoryViewModel = categoryViewModel,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = {
                                    themeViewModel.toggleTheme(isDarkTheme)
                                }
                            )
                        }
                        composable(Screen.TransactionList.route) {
                            TransactionListScreen(
                                viewModel = txViewModel,
                                walletViewModel = walletViewModel,
                                categoryViewModel = categoryViewModel,
                                personViewModel = personViewModel,
                                tagViewModel = tagViewModel,
                                onAddTransactionClick = {
                                    navController.navigate(Screen.AddTransaction.route)
                                }
                            )
                        }
                        composable(Screen.AddTransaction.route) {
                            AddTransactionScreen(
                                viewModel = txViewModel,
                                walletViewModel = walletViewModel,
                                categoryViewModel = categoryViewModel,
                                personViewModel = personViewModel,
                                tagViewModel = tagViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.Wallets.route) {
                            WalletScreen(
                                walletViewModel = walletViewModel,
                                txViewModel = txViewModel,
                                onBackClick = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Expense Reminder"
            val descriptionText = "Reminds you to log your daily expenses"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("expense_reminder", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
