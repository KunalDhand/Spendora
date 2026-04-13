package com.example.testing.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.ui.theme.getExpenseColor

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
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Income", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("₹${"%.2f".format(income)}", style = MaterialTheme.typography.bodyLarge, color = incomeColor, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expense", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("₹${"%.2f".format(expense)}", style = MaterialTheme.typography.bodyLarge, color = expenseColor, fontWeight = FontWeight.Bold)
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net Balance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "₹${"%.2f".format(net)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (net >= 0) incomeColor else expenseColor
                )
            }
        }
    }
}
