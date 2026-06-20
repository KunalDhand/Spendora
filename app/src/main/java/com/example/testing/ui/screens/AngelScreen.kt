package com.example.testing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
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
fun AngelScreen(
    transactionViewModel: TransactionViewModel,
    personViewModel: PersonViewModel,
    onNavigateBack: () -> Unit,
    onSwitchToCredit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val angelBalances by transactionViewModel.getPersonAngelBalances().collectAsState(initial = emptyList())
    val allPersons by personViewModel.persons.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Angel Splitting", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSwitchToCredit) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Credits")
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
            item {
                AngelSummaryCard(angelBalances)
            }

            item {
                Text(
                    "Generosity Balance per Person",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (angelBalances.isEmpty()) {
                item {
                    Text(
                        "No splitting records yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(angelBalances) { balance ->
                    val person = allPersons.find { it.id == balance.personId }
                    PersonAngelItem(person, balance.angelBalance)
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AngelSummaryCard(balances: List<com.example.testing.data.local.PersonAngelEntity>) {
    val totalISpentOnOthers = balances.filter { it.angelBalance > 0 }.sumOf { it.angelBalance }
    val totalOthersSpentOnMe = balances.filter { it.angelBalance < 0 }.sumOf { abs(it.angelBalance) }
    val netStatus = totalISpentOnOthers - totalOthersSpentOnMe

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("I Paid for Others", style = MaterialTheme.typography.labelMedium)
                    val formattedISpent = String.format(Locale.getDefault(), "%.2f", totalISpentOnOthers)
                    Text(
                        "₹$formattedISpent",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = getIncomeColor()
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Others Paid for Me", style = MaterialTheme.typography.labelMedium)
                    val formattedOthersSpent = String.format(Locale.getDefault(), "%.2f", totalOthersSpentOnMe)
                    Text(
                        "₹$formattedOthersSpent",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = getExpenseColor()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                val formattedNet = String.format(Locale.getDefault(), "%.2f", abs(netStatus))
                Text(
                    if (netStatus >= 0.0) "Net Generosity: +₹$formattedNet"
                    else "Net Received: ₹$formattedNet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PersonAngelItem(person: PersonEntity?, balance: Double) {
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
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(person?.name ?: "Unknown", fontWeight = FontWeight.Bold)
                Text(
                    if (balance > 0) "You paid for them" else "They paid for you",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val formattedBalance = String.format(Locale.getDefault(), "%.2f", abs(balance))
            Text(
                "₹$formattedBalance",
                fontWeight = FontWeight.ExtraBold,
                color = if (balance > 0) getIncomeColor() else getExpenseColor()
            )
        }
    }
}
