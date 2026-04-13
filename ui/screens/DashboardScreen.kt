package com.example.testing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testing.ui.viewmodel.TransactionViewModel

@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    onViewTransactionsClick: () -> Unit,
    onViewWalletsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBalance by viewModel.totalBalance.collectAsState(initial = 0.0)
    
    val todayIncome by viewModel.todayIncome.collectAsState(initial = 0.0)
    val todayExpense by viewModel.todayExpense.collectAsState(initial = 0.0)
    val todayNet by viewModel.todayNet.collectAsState(initial = 0.0)

    val monthlyIncome by viewModel.monthlyIncome.collectAsState(initial = 0.0)
    val monthlyExpense by viewModel.monthlyExpense.collectAsState(initial = 0.0)
    val monthlyNet by viewModel.monthlyNet.collectAsState(initial = 0.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Total Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Balance", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "₹${"%.2f".format(totalBalance ?: 0.0)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Today's Summary
        SummarySection(
            title = "Today",
            income = todayIncome ?: 0.0,
            expense = todayExpense ?: 0.0,
            net = todayNet ?: 0.0
        )

        // Monthly Summary
        SummarySection(
            title = "This Month",
            income = monthlyIncome ?: 0.0,
            expense = monthlyExpense ?: 0.0,
            net = monthlyNet ?: 0.0
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onViewTransactionsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View All Transactions")
        }

        OutlinedButton(
            onClick = onViewWalletsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Wallets")
        }
    }
}

@Composable
fun SummarySection(
    title: String,
    income: Double,
    expense: Double,
    net: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Income", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("₹${"%.2f".format(income)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32)) // Dark Green
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expense", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("₹${"%.2f".format(expense)}", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
            }
            
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "₹${"%.2f".format(net)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (net >= 0) Color(0xFF2E7D32) else Color.Red
                )
            }
        }
    }
}
