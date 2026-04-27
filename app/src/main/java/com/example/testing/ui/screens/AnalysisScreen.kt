package com.example.testing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testing.data.local.DailyExpense
import com.example.testing.data.local.NetData
import com.example.testing.ui.components.CategorySummaryCard
import com.example.testing.ui.components.LineChartView
import com.example.testing.ui.components.NetLineChart
import com.example.testing.ui.components.PieChartView
import com.example.testing.ui.viewmodel.CategoryUI
import com.example.testing.ui.viewmodel.CategoryViewModel
import com.example.testing.ui.viewmodel.TransactionViewModel
import com.example.testing.utils.TimeFrame
import com.example.testing.utils.TimeFrameUtils

enum class NetTimeFrame {
    DAILY, WEEKLY, MONTHLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by categoryViewModel.categories.collectAsState(initial = emptyList())
    val categoryListRaw by viewModel.getCategoryUIList(categories).collectAsState(initial = emptyList())
    val categoryList = remember(categoryListRaw) {
        categoryListRaw.sortedByDescending { it.total }
    }

    val totalExpenseValue = remember(categoryList) {
        categoryList.sumOf { it.total }
    }

    // Expense Trend States
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.MONTH) }
    val (start, end) = remember(selectedTimeFrame) { TimeFrameUtils.getTimeFrameRange(selectedTimeFrame) }
    val dailyExpenses by viewModel.getDailyExpensesByRange(start, end).collectAsState(initial = emptyList())

    // PnL States
    var selectedNetFrame by remember { mutableStateOf(NetTimeFrame.DAILY) }
    val dailyNet by viewModel.getDailyNet().collectAsState(initial = emptyList())
    val weeklyNet by viewModel.getWeeklyNet().collectAsState(initial = emptyList())
    val monthlyNet by viewModel.getMonthlyNetList().collectAsState(initial = emptyList())

    val netData = when (selectedNetFrame) {
        NetTimeFrame.DAILY -> dailyNet
        NetTimeFrame.WEEKLY -> weeklyNet
        NetTimeFrame.MONTHLY -> monthlyNet
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Analysis", fontWeight = FontWeight.ExtraBold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Pie Chart Section (Category Breakdown)
            if (categoryList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "CATEGORY BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PieChartView(data = categoryList)

                    val topCategory = categoryList.first()
                    val percentage = if (totalExpenseValue > 0) (topCategory.total / totalExpenseValue) * 100 else 0.0
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "💡 ${topCategory.name} accounts for ${percentage.toInt()}% of your total spending.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "TOP CATEGORIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                    
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        categoryList.forEach { category ->
                            CategorySummaryCard(
                                name = category.name,
                                amount = category.total,
                                totalExpense = totalExpenseValue
                            )
                        }
                    }
                }

            }

            HorizontalDivider()

            // PnL Section (Net Income)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "NET INCOME (PNL)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NetTimeFrame.entries.forEach { tf ->
                        val isSelected = selectedNetFrame == tf
                        Surface(
                            onClick = { selectedNetFrame = tf },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = tf.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                if (netData.isNotEmpty()) {
                    NetLineChart(data = netData)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                            Text(text = "No PnL data available", modifier = Modifier.align(androidx.compose.ui.Alignment.Center), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            HorizontalDivider()

            // Spending Trend Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "SPENDING TREND",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                
                // TimeFrame Selector
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeFrame.entries.forEach { tf ->
                        val isSelected = selectedTimeFrame == tf
                        Surface(
                            onClick = { selectedTimeFrame = tf },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = tf.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                if (dailyExpenses.isNotEmpty()) {
                    LineChartView(data = dailyExpenses, timeFrame = selectedTimeFrame)

                    val maxDay = dailyExpenses.maxByOrNull { it.total }
                    Text(
                        text = "Highest spending: ₹${"%.2f".format(maxDay?.total ?: 0.0)} on ${maxDay?.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
