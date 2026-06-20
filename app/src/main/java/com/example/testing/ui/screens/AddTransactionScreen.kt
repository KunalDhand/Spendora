package com.example.testing.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
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
import com.example.testing.data.local.TransactionSplitEntity
import com.example.testing.data.local.WalletEntity
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.ui.theme.getTransferColor
import com.example.testing.ui.viewmodel.CategoryViewModel
import com.example.testing.ui.viewmodel.PersonViewModel
import com.example.testing.ui.viewmodel.TagViewModel
import com.example.testing.ui.viewmodel.TransactionViewModel
import com.example.testing.ui.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.*

data class SplitItem(
    var person: PersonEntity? = null,
    var amount: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    walletViewModel: WalletViewModel,
    categoryViewModel: CategoryViewModel,
    personViewModel: PersonViewModel,
    tagViewModel: TagViewModel,
    onNavigateBack: () -> Unit,
    editTransactionId: Int? = null,
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

    var toWalletExpanded by remember { mutableStateOf(false) }
    var selectedToWallet by remember { mutableStateOf<WalletEntity?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    var personExpanded by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<PersonEntity?>(null) }
    var showAddPersonDialog by remember { mutableStateOf(false) }

    var tagExpanded by remember { mutableStateOf(false) }
    val selectedTags = remember { mutableStateListOf<TagEntity>() }
    var showAddTagDialog by remember { mutableStateOf(false) }

    var isCredit by remember { mutableStateOf(false) }

    // Advanced Split Logic
    var isAdvanceEnabled by remember { mutableStateOf(false) }
    var whoPaidUser by remember { mutableStateOf(true) } // true = "You" paid, false = "Someone Else" paid
    val splits = remember { mutableStateListOf<SplitItem>() }

    // Pre-fill logic for edit mode
    LaunchedEffect(editTransactionId, wallets, categories, persons, allTags) {
        if (editTransactionId != null && wallets.isNotEmpty()) {
            val tx = viewModel.getTransactionEntityById(editTransactionId)
            if (tx != null) {
                amount = tx.amount.toString()
                transactionType = tx.type
                note = tx.note ?: ""
                selectedDateTime = tx.timestamp
                selectedWallet = wallets.find { it.id == tx.walletId }
                selectedToWallet = wallets.find { it.id == tx.toWalletId }
                selectedCategory = categories.find { it.id == tx.categoryId }
                selectedPerson = persons.find { it.id == tx.personId }
                isCredit = tx.isCredit
                
                // Fetch tags
                val tags = viewModel.repository.getTagsForTransactionOnce(tx.id)
                selectedTags.clear()
                selectedTags.addAll(tags)

                // Fetch splits
                val existingSplits = viewModel.repository.getSplitsForTransaction(tx.id)
                if (existingSplits.isNotEmpty()) {
                    isAdvanceEnabled = true
                    whoPaidUser = existingSplits.first().isLent
                    splits.clear()
                    existingSplits.forEach { s ->
                        val p = persons.find { it.id == s.personId }
                        splits.add(SplitItem(p, s.amount.toString()))
                    }
                }
            }
        }
    }

    // Validation
    val totalAmount = amount.toDoubleOrNull() ?: 0.0
    val isAmountValid = totalAmount > 0
    
    val splitSum = splits.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val areSplitsValid = if (isAdvanceEnabled && transactionType == "EXPENSE") {
        splits.isNotEmpty() && splits.all { it.person != null && (it.amount.toDoubleOrNull() ?: 0.0) > 0 } &&
                (splitSum <= totalAmount + 0.0001)
    } else true

    val isFormValid = if (transactionType == "TRANSFER") {
        isAmountValid && selectedWallet != null && selectedToWallet != null && selectedWallet != selectedToWallet
    } else {
        isAmountValid && selectedWallet != null && areSplitsValid
    }

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

    if (showAddCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Create New Category", fontWeight = FontWeight.ExtraBold) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name (e.g. Health)") },
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
                        if (newCategoryName.isNotBlank()) {
                            categoryViewModel.addCategory(newCategoryName)
                            showAddCategoryDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { 
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
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
        Text(
            if (editTransactionId == null) "Add Transaction" else "Edit Transaction",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        // Type Toggle
        Row(modifier = Modifier.fillMaxWidth()) {
            val incomeColor = getIncomeColor()
            val expenseColor = getExpenseColor()
            val transferColor = getTransferColor()
            
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
                onClick = { 
                    transactionType = "INCOME"
                    isAdvanceEnabled = false
                },
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (transactionType == "INCOME") incomeColor else MaterialTheme.colorScheme.surface,
                border = if (transactionType == "INCOME") null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Income", color = if (transactionType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                }
            }

            Surface(
                onClick = { 
                    transactionType = "TRANSFER"
                    isAdvanceEnabled = false
                    selectedCategory = categories.find { it.name.equals("Transfer", ignoreCase = true) }
                    selectedPerson = persons.find { it.name.equals("TRANSFER", ignoreCase = true) }
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (transactionType == "TRANSFER") transferColor else MaterialTheme.colorScheme.surface,
                border = if (transactionType == "TRANSFER") null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Transfer", color = if (transactionType == "TRANSFER") Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Wallet Selector (From)
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { walletExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(if (transactionType == "TRANSFER") "From Wallet" else "Wallet", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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

            if (transactionType == "TRANSFER") {
                // Destination Wallet Selector (To)
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        onClick = { toWalletExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("To Wallet", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = selectedToWallet?.name ?: "Select", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    DropdownMenu(expanded = toWalletExpanded, onDismissRequest = { toWalletExpanded = false }) {
                        wallets.forEach { wallet ->
                            DropdownMenuItem(
                                text = { Text(wallet.name) },
                                onClick = {
                                    selectedToWallet = wallet
                                    toWalletExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
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
                            Text(text = selectedCategory?.name ?: "None (Optional)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }

                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedCategory = null
                                categoryExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Add New") },
                            onClick = {
                                showAddCategoryDialog = true
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (transactionType != "TRANSFER") {
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
        }

        // Tags Multi-Select
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
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTags.forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tag.name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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

        if (tagExpanded) {
            ModalBottomSheet(
                onDismissRequest = { tagExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Tags", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        TextButton(
                            onClick = { showAddTagDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New Tag", fontWeight = FontWeight.Bold)
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        allTags.forEach { tag ->
                            val isSelected = selectedTags.any { it.id == tag.id }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedTags.removeAll { it.id == tag.id }
                                    } else {
                                        selectedTags.add(tag)
                                    }
                                },
                                label = { Text(tag.name) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.dp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { tagExpanded = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.ExtraBold)
                    }
                }
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
            isError = (amount.isNotEmpty() && !isAmountValid) || (isAdvanceEnabled && transactionType == "EXPENSE" && splitSum > totalAmount + 0.0001),
            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
        if (amount.isNotEmpty() && !isAmountValid) {
            Text("Please enter a valid amount greater than 0", color = getExpenseColor(), style = MaterialTheme.typography.bodySmall)
        } else if (isAdvanceEnabled && transactionType == "EXPENSE" && splitSum > totalAmount + 0.0001) {
            Text("Total split amount exceeds transaction amount", color = getExpenseColor(), style = MaterialTheme.typography.bodySmall)
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

        // Advance Split Logic Toggle
        if (transactionType == "EXPENSE") {
            Surface(
                onClick = { 
                    isAdvanceEnabled = !isAdvanceEnabled
                    if (isAdvanceEnabled) isCredit = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isAdvanceEnabled) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isAdvanceEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isAdvanceEnabled) Icons.Default.AddCircle else Icons.Default.AddCircle, 
                        contentDescription = null,
                        tint = if (isAdvanceEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Split with Others", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Divide this amount among multiple people", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isAdvanceEnabled, 
                        onCheckedChange = { 
                            isAdvanceEnabled = it
                            if (it) isCredit = false
                        }
                    )
                }
            }
        }

        if (isAdvanceEnabled && transactionType == "EXPENSE") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Payment Direction
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { whoPaidUser = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (whoPaidUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("You Paid", color = if (whoPaidUser) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            onClick = { whoPaidUser = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (!whoPaidUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("Someone Else Paid", color = if (!whoPaidUser) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Splits", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    splits.forEachIndexed { index, split ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var splitPersonExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1.5f)) {
                                OutlinedTextField(
                                    value = split.person?.name ?: "Select Person",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { splitPersonExpanded = true }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = null)
                                        }
                                    }
                                )
                                DropdownMenu(expanded = splitPersonExpanded, onDismissRequest = { splitPersonExpanded = false }) {
                                    persons.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.name) },
                                            onClick = {
                                                splits[index] = split.copy(person = p)
                                                splitPersonExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = split.amount,
                                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) splits[index] = split.copy(amount = it) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp)
                            )

                            IconButton(onClick = { splits.removeAt(index) }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { splits.add(SplitItem()) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Person")
                        }

                        Button(
                            onClick = {
                                if (totalAmount > 0) {
                                    val count = splits.size + 1
                                    val equalAmount = String.format(Locale.US, "%.2f", totalAmount / count)
                                    splits.forEachIndexed { i, s ->
                                        splits[i] = s.copy(amount = equalAmount)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Equal Split")
                        }
                    }

                    if (isAdvanceEnabled && transactionType == "EXPENSE" && splitSum > totalAmount + 0.0001) {
                        Text(
                            "Total split amount (₹$splitSum) cannot exceed transaction amount (₹$totalAmount)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (isAdvanceEnabled && transactionType == "EXPENSE" && splits.isEmpty()) {
                        Text(
                            "Add at least one person to split with",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Credit Checkbox
        if (transactionType != "TRANSFER") {
            Surface(
                onClick = { 
                    isCredit = !isCredit 
                    if (isCredit) isAdvanceEnabled = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isCredit) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = isCredit,
                        onCheckedChange = { 
                            isCredit = it 
                            if (it) isAdvanceEnabled = false
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Column {
                        Text(
                            text = if (transactionType == "EXPENSE") "Lent money (Loan Given)" else "Borrowed money (Debt Taken)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mark this transaction as a credit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!isFormValid) return@Button
                
                val amountDouble = amount.toDoubleOrNull() ?: 0.0

                val splitEntities = if (isAdvanceEnabled && transactionType == "EXPENSE") {
                    splits.filter { it.person != null && it.amount.toDoubleOrNull() != null }.map {
                        TransactionSplitEntity(
                            transactionId = editTransactionId ?: 0,
                            personId = it.person!!.id,
                            amount = it.amount.toDouble(),
                            isLent = whoPaidUser
                        )
                    }
                } else emptyList()

                // Enhanced Note for Split (appends breakdown if You Paid)
                var finalNote = note
                if (transactionType == "EXPENSE" && whoPaidUser && splitEntities.isNotEmpty()) {
                    val splitSum = splitEntities.sumOf { it.amount }
                    val myShare = amountDouble - splitSum
                    
                    val splitNoteBuilder = StringBuilder()
                    val shareList = splitEntities.map { it.amount } + myShare
                    val isEquallySplit = shareList.all { kotlin.math.abs(it - shareList[0]) < 0.01 }
                    
                    if (isEquallySplit) splitNoteBuilder.append("Equal split\n")
                    
                    val myShareStr = if (myShare % 1.0 == 0.0) myShare.toInt().toString() else String.format(Locale.US, "%.2f", myShare)
                    splitNoteBuilder.append("me = $myShareStr\n")
                    
                    splitEntities.forEach { entity ->
                        val pName = persons.find { it.id == entity.personId }?.name ?: "Unknown"
                        val sAmtStr = if (entity.amount % 1.0 == 0.0) entity.amount.toInt().toString() else entity.amount.toString()
                        splitNoteBuilder.append("$pName = $sAmtStr\n")
                    }
                    
                    val generatedNote = splitNoteBuilder.toString().trim()
                    if (!finalNote.contains(generatedNote)) {
                        if (finalNote.isNotEmpty()) {
                            finalNote += if (finalNote.endsWith("\n")) "\n" else "\n\n"
                        }
                        finalNote += generatedNote
                    }
                }

                val transaction = TransactionEntity(
                    id = editTransactionId ?: 0,
                    amount = amountDouble,
                    walletId = selectedWallet!!.id,
                    toWalletId = if (transactionType == "TRANSFER") selectedToWallet?.id else null,
                    categoryId = selectedCategory?.id,
                    personId = selectedPerson?.id,
                    type = transactionType,
                    note = finalNote,
                    timestamp = selectedDateTime,
                    isCredit = if (transactionType == "TRANSFER") false else isCredit
                )

                if (editTransactionId == null) {
                    viewModel.addTransaction(transaction, selectedTags.map { it.id }, splitEntities)
                } else {
                    viewModel.updateTransaction(transaction, selectedTags.map { it.id }, splitEntities)
                }
                onNavigateBack()
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (transactionType) {
                    "EXPENSE" -> getExpenseColor()
                    "INCOME" -> getIncomeColor()
                    "TRANSFER" -> getTransferColor()
                    else -> MaterialTheme.colorScheme.primary
                },
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            val buttonText = if (editTransactionId == null) {
                when (transactionType) {
                    "EXPENSE" -> "Save Expense"
                    "INCOME" -> "Save Income"
                    else -> "Save Transfer"
                }
            } else {
                "Update Transaction"
            }
            Text(buttonText, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(8.dp))
        }
    }
}
