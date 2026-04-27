package com.example.testing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testing.data.local.PersonEntity
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.ui.viewmodel.PersonViewModel
import com.example.testing.ui.viewmodel.TransactionViewModel
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditScreen(
    transactionViewModel: TransactionViewModel,
    personViewModel: PersonViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val creditTransactions by transactionViewModel.getCreditTransactionsUI().collectAsState(initial = emptyList())
    val personBalances by transactionViewModel.getPersonCreditBalances().collectAsState(initial = emptyList())
    val allPersons by personViewModel.persons.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits & Debts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Section
            item {
                CreditSummaryCard(personBalances)
            }

            // People Balances Section
            item {
                Text(
                    "People",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (personBalances.isEmpty()) {
                item {
                    Text(
                        "No pending credits.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(personBalances) { balance ->
                    val person = allPersons.find { it.id == balance.personId }
                    PersonCreditItem(person, balance.creditBalance)
                }
            }

            // Recent Credit Transactions Section
            item {
                Text(
                    "Recent Credit Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(creditTransactions) { tx ->
                CreditTransactionItem(tx)
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun CreditSummaryCard(balances: List<com.example.testing.data.local.PersonCreditEntity>) {
    val totalLent = balances.filter { it.creditBalance < 0 }.sumOf { abs(it.creditBalance) }
    val totalBorrowed = balances.filter { it.creditBalance > 0 }.sumOf { it.creditBalance }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Lent", style = MaterialTheme.typography.labelMedium)
                Text(
                    "₹${String.format(Locale.getDefault(), "%.2f", totalLent)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = getExpenseColor()
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Borrowed", style = MaterialTheme.typography.labelMedium)
                Text(
                    "₹${String.format(Locale.getDefault(), "%.2f", totalBorrowed)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = getIncomeColor()
                )
            }
        }
    }
}

@Composable
fun PersonCreditItem(person: PersonEntity?, balance: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(person?.name ?: "Unknown", fontWeight = FontWeight.Bold)
                Text(
                    if (balance < 0) "You lent money" else "You borrowed money",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "₹${String.format(Locale.getDefault(), "%.2f", abs(balance))}",
                fontWeight = FontWeight.ExtraBold,
                color = if (balance < 0) getExpenseColor() else getIncomeColor()
            )
        }
    }
}

@Composable
fun CreditTransactionItem(tx: com.example.testing.ui.viewmodel.TransactionUI) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.person ?: "Unknown", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(tx.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format(Locale.getDefault(), "%.2f", tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (tx.type == "EXPENSE") getExpenseColor() else getIncomeColor()
                )
                Text(
                    if (tx.type == "EXPENSE") "Lent" else "Borrowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
