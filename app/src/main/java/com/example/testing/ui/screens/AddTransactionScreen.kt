package com.example.testing.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.testing.data.local.CategoryEntity
import com.example.testing.data.local.PersonEntity
import com.example.testing.data.local.TagEntity
import com.example.testing.data.local.TransactionEntity
import com.example.testing.data.local.WalletEntity
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.ui.viewmodel.CategoryViewModel
import com.example.testing.ui.viewmodel.PersonViewModel
import com.example.testing.ui.viewmodel.TagViewModel
import com.example.testing.ui.viewmodel.TransactionViewModel
import com.example.testing.ui.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    walletViewModel: WalletViewModel,
    categoryViewModel: CategoryViewModel,
    personViewModel: PersonViewModel,
    tagViewModel: TagViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") }
    var note by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val wallets by walletViewModel.wallets.collectAsState(initial = emptyList())
    val categories by categoryViewModel.categories.collectAsState(initial = emptyList())
    val persons by personViewModel.persons.collectAsState(initial = emptyList())
    val allTags by tagViewModel.allTags.collectAsState(initial = emptyList())
    
    var walletExpanded by remember { mutableStateOf(false) }
    var selectedWallet by remember { mutableStateOf<WalletEntity?>(null) }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var personExpanded by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<PersonEntity?>(null) }
    var showAddPersonDialog by remember { mutableStateOf(false) }

    var tagExpanded by remember { mutableStateOf(false) }
    val selectedTags = remember { mutableStateListOf<TagEntity>() }
    var showAddTagDialog by remember { mutableStateOf(false) }

    // Validation
    val isAmountValid = amount.toDoubleOrNull()?.let { it > 0 } ?: false
    val isFormValid = isAmountValid && selectedWallet != null && selectedCategory != null

    // Date Picker Dialog
    if (showDatePicker) {
        calendar.timeInMillis = selectedDateTime
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.timeInMillis = selectedDateTime
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                selectedDateTime = calendar.timeInMillis
                showDatePicker = false
                showTimePicker = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
        showDatePicker = false
    }

    // Time Picker Dialog
    if (showTimePicker) {
        calendar.timeInMillis = selectedDateTime
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.timeInMillis = selectedDateTime
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                selectedDateTime = calendar.timeInMillis
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
        showTimePicker = false
    }

    if (showAddPersonDialog) {
        var newPersonName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("Add Person/Entity", fontWeight = FontWeight.ExtraBold) },
            text = {
                OutlinedTextField(
                    value = newPersonName,
                    onValueChange = { newPersonName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPersonName.isNotBlank()) {
                            personViewModel.addPerson(newPersonName)
                            showAddPersonDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showAddTagDialog) {
        var newTagName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("Create New Tag", fontWeight = FontWeight.ExtraBold) },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("Tag Name (e.g. #Work)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            tagViewModel.addTag(newTagName)
                            showAddTagDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Add Transaction", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        // Type Toggle
        Row(modifier = Modifier.fillMaxWidth()) {
            val incomeColor = getIncomeColor()
            val expenseColor = getExpenseColor()
            
            Surface(
                onClick = { transactionType = "EXPENSE" },
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (transactionType == "EXPENSE") expenseColor else MaterialTheme.colorScheme.surface,
                border = if (transactionType == "EXPENSE") null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Expense", color = if (transactionType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            Surface(
                onClick = { transactionType = "INCOME" },
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (transactionType == "INCOME") incomeColor else MaterialTheme.colorScheme.surface,
                border = if (transactionType == "INCOME") null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Income", color = if (transactionType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Wallet Selector
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { walletExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Wallet", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(text = selectedWallet?.name ?: "Select", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }

                DropdownMenu(expanded = walletExpanded, onDismissRequest = { walletExpanded = false }) {
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text(wallet.name) },
                            onClick = {
                                selectedWallet = wallet
                                walletExpanded = false
                            }
                        )
                    }
                }
            }

            // Category Selector
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { categoryExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(text = selectedCategory?.name ?: "Select", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }

                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Person / Company Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { personExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payee / Payer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = selectedPerson?.name ?: "None (Optional)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            DropdownMenu(expanded = personExpanded, onDismissRequest = { personExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        selectedPerson = null
                        personExpanded = false
                    }
                )
                persons.forEach { person ->
                    DropdownMenuItem(
                        text = { Text(person.name) },
                        onClick = {
                            selectedPerson = person
                            personExpanded = false
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("+ Add New") },
                    onClick = {
                        showAddPersonDialog = true
                        personExpanded = false
                    }
                )
            }
        }

        // Tags Multi-Select
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { tagExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tags", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (selectedTags.isEmpty()) {
                        Text("Select Tags (Optional)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(selectedTags) { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = tagExpanded,
                onDismissRequest = { tagExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                allTags.forEach { tag ->
                    val isSelected = selectedTags.any { it.id == tag.id }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                                Spacer(Modifier.width(8.dp))
                                Text(tag.name)
                            }
                        },
                        onClick = {
                            if (isSelected) {
                                selectedTags.removeAll { it.id == tag.id }
                            } else {
                                selectedTags.add(tag)
                            }
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("+ Add New Tag") },
                    onClick = {
                        showAddTagDialog = true
                        tagExpanded = false
                    }
                )
            }
        }

        // Date & Time Picker Card
        Surface(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Date & Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "📅 ${dateFormatter.format(Date(selectedDateTime))}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            isError = amount.isNotEmpty() && !isAmountValid,
            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
        if (amount.isNotEmpty() && !isAmountValid) {
            Text("Please enter a valid amount greater than 0", color = getExpenseColor(), style = MaterialTheme.typography.bodySmall)
        }

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )

        Button(
            onClick = {
                if (!isFormValid) return@Button
                
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                val transaction = TransactionEntity(
                    amount = amountDouble,
                    walletId = selectedWallet!!.id,
                    categoryId = selectedCategory!!.id,
                    personId = selectedPerson?.id,
                    type = transactionType,
                    note = note,
                    timestamp = selectedDateTime
                )

                viewModel.addTransaction(transaction, selectedTags.map { it.id })
                onNavigateBack()
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (transactionType == "EXPENSE") getExpenseColor() else getIncomeColor(),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(if (transactionType == "EXPENSE") "Save Expense" else "Save Income", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(8.dp))
        }
    }
}
