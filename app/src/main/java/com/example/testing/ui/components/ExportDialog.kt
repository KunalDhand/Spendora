package com.example.testing.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testing.utils.DateFilter
import java.util.*

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (DateFilter, Long?, Long?) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(DateFilter.THIS_MONTH) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Export Transactions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            ) 
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Select a period to export your transaction history as a PDF report.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                DateFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        onClick = { selectedFilter = filter },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = filter.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedFilter == DateFilter.CUSTOM) {
                        // Start Date Picker
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day, 0, 0, 0)
                                val startDate = calendar.timeInMillis

                                // End Date Picker
                                DatePickerDialog(
                                    context,
                                    { _, endYear, endMonth, endDay ->
                                        calendar.set(endYear, endMonth, endDay, 23, 59, 59)
                                        val endDate = calendar.timeInMillis
                                        onExport(DateFilter.CUSTOM, startDate, endDate)
                                        onDismiss()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    } else {
                        onExport(selectedFilter, null, null)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Export PDF", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
