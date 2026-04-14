package com.example.testing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testing.ui.components.*
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onViewTransactionsClick: () -> Unit,
    onViewWalletsClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportDialog by remember { mutableStateOf(false) }

    val totalBalance by viewModel.totalBalance.collectAsState(initial = 0.0)
    
    val todayIncome by viewModel.todayIncome.collectAsState(initial = 0.0)
    val todayExpense by viewModel.todayExpense.collectAsState(initial = 0.0)
    val todayNet by viewModel.todayNet.collectAsState(initial = 0.0)
    
    val monthlyIncome by viewModel.monthlyIncome.collectAsState(initial = 0.0)
    val monthlyExpense by viewModel.monthlyExpense.collectAsState(initial = 0.0)
    val monthlyNet by viewModel.monthlyNet.collectAsState(initial = 0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Balance Section (Fintech Style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TOTAL BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₹${"%.2f".format(totalBalance ?: 0.0)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                "Financial Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Today Section (High-Density)
            SummaryCard(
                title = "Today",
                income = todayIncome ?: 0.0,
                expense = todayExpense ?: 0.0,
                net = todayNet ?: 0.0
            )

            // Monthly Section (High-Density)
            SummaryCard(
                title = "This Month",
                income = monthlyIncome ?: 0.0,
                expense = monthlyExpense ?: 0.0,
                net = monthlyNet ?: 0.0
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onViewTransactionsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Transactions", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onViewWalletsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text("Wallets", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = { showExportDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export PDF Report", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { filter, customStart, customEnd ->
                scope.launch {
                    viewModel.exportToPdf(context, filter, customStart, customEnd)
                    showExportDialog = false
                }
            }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    income: Double,
    expense: Double,
    net: Double
) {
    val incomeColor = getIncomeColor()
    val expenseColor = getExpenseColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Surface(
                    color = (if (net >= 0) incomeColor else expenseColor).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "₹${"%.2f".format(net)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) incomeColor else expenseColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${"%.2f".format(income)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = incomeColor)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${"%.2f".format(expense)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = expenseColor)
                }
            }
        }
    }
}

