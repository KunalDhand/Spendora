package com.example.testing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import com.example.testing.ui.viewmodel.CategoryViewModel
import com.example.testing.ui.viewmodel.PersonViewModel
import com.example.testing.ui.viewmodel.TagViewModel
import com.example.testing.ui.viewmodel.TransactionUI
import com.example.testing.ui.viewmodel.TransactionViewModel
import com.example.testing.ui.viewmodel.WalletViewModel

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

// Design System Colors removed - now using MaterialTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel,
    walletViewModel: WalletViewModel,
    categoryViewModel: CategoryViewModel,
    personViewModel: PersonViewModel,
    tagViewModel: TagViewModel,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.getTransactionsUI().collectAsState(initial = null)
    val allTags by tagViewModel.allTags.collectAsState(initial = emptyList())
    val allPersons by personViewModel.persons.collectAsState(initial = emptyList())
    val allCategories by categoryViewModel.categories.collectAsState(initial = emptyList())
    val allWallets by walletViewModel.wallets.collectAsState(initial = emptyList())

    val selectedTagIds = remember { mutableStateListOf<Int>() }
    val selectedPersonIds = remember { mutableStateListOf<Int>() }
    val selectedCategoryIds = remember { mutableStateListOf<Int>() }
    val selectedWalletIds = remember { mutableStateListOf<Int>() }
    var searchQuery by remember { mutableStateOf("") }
    
    // New Filter States
    var personQuery by remember { mutableStateOf("") }
    var walletQuery by remember { mutableStateOf("") }
    var categoryQuery by remember { mutableStateOf("") }
    var tagQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    
    var txToDelete by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val keywords = searchQuery.lowercase().split(" ").filter { it.isNotBlank() }

    val filteredTransactions = transactions?.filter { tx ->
        val matchesFilters = (selectedTagIds.isEmpty() || tx.tagIds.any { it in selectedTagIds }) &&
                (selectedPersonIds.isEmpty() || tx.personId in selectedPersonIds) &&
                (selectedCategoryIds.isEmpty() || tx.categoryId in selectedCategoryIds) &&
                (selectedWalletIds.isEmpty() || tx.walletId in selectedWalletIds)

        if (!matchesFilters) return@filter false

        if (keywords.isEmpty()) return@filter true

        keywords.all { keyword ->
            val matchPerson = tx.person?.lowercase()?.contains(keyword) == true
            val matchCategory = tx.category.lowercase().contains(keyword)
            val matchWallet = tx.wallet.lowercase().contains(keyword)
            val matchNote = tx.note?.lowercase()?.contains(keyword) == true
            val matchTags = tx.tags.any { it.lowercase().contains(keyword) }
            val matchAmount = tx.amount.toString().contains(keyword)

            matchPerson || matchCategory || matchWallet || matchNote || matchTags || matchAmount
        }
    }

    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This will also update your wallet balance.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = txToDelete!!
                        scope.launch {
                            val entity = viewModel.getTransactionEntityById(id)
                            if (entity != null) {
                                viewModel.deleteTransaction(id)
                                txToDelete = null
                                
                                val result = snackbarHostState.showSnackbar(
                                    message = "Transaction deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Long
                                )
                                
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreTransaction(entity)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = getExpenseColor())
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold, // Premium Fintech look
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search (e.g. rahul food 500)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Modern Filter Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FilterList, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    val activeCount = selectedPersonIds.size + selectedWalletIds.size + selectedCategoryIds.size + selectedTagIds.size
                    Text(
                        text = if (activeCount > 0) "Filters ($activeCount)" else "Filters", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                TextButton(onClick = { showFilters = !showFilters }) {
                    Text(if (showFilters) "Hide" else "Show All", fontWeight = FontWeight.Bold)
                    Icon(
                        if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    // Person Filter (1 per row - List Style)
                    FilterGroup(title = "Person", selectedCount = selectedPersonIds.size) {
                        OutlinedTextField(
                            value = personQuery,
                            onValueChange = { personQuery = it },
                            placeholder = { Text("Search person...") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        allPersons.filter { it.name.contains(personQuery, ignoreCase = true) }.forEach { person ->
                            val isSelected = person.id in selectedPersonIds
                            FilterChip(
                                text = person.name,
                                selected = isSelected,
                                modifier = Modifier.fillMaxWidth(), // 1 per row
                                onClick = {
                                    if (isSelected) selectedPersonIds.remove(person.id)
                                    else selectedPersonIds.add(person.id)
                                }
                            )
                        }
                    }

                    // Tags (Flow Layout - Wrap)
                    FilterGroup(title = "Tags", selectedCount = selectedTagIds.size) {
                        OutlinedTextField(
                            value = tagQuery,
                            onValueChange = { tagQuery = it },
                            placeholder = { Text("Search tags...") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val filteredTags = allTags.filter { it.name.contains(tagQuery, ignoreCase = true) }
                            filteredTags.chunked(3).forEach { rowTags ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    rowTags.forEach { tag ->
                                        val isSelected = tag.id in selectedTagIds
                                        FilterChip(
                                            text = tag.name,
                                            selected = isSelected,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                if (isSelected) selectedTagIds.remove(tag.id)
                                                else selectedTagIds.add(tag.id)
                                            }
                                        )
                                    }
                                    // Fill empty space in incomplete rows
                                    repeat(3 - rowTags.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Wallet Filter (1 per row - List Style)
                    FilterGroup(title = "Wallet", selectedCount = selectedWalletIds.size) {
                        OutlinedTextField(
                            value = walletQuery,
                            onValueChange = { walletQuery = it },
                            placeholder = { Text("Search wallet...") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        allWallets.filter { it.name.contains(walletQuery, ignoreCase = true) }.forEach { wallet ->
                            val isSelected = wallet.id in selectedWalletIds
                            FilterChip(
                                text = wallet.name,
                                selected = isSelected,
                                modifier = Modifier.fillMaxWidth(), // 1 per row
                                onClick = {
                                    if (isSelected) selectedWalletIds.remove(wallet.id)
                                    else selectedWalletIds.add(wallet.id)
                                }
                            )
                        }
                    }

                    // Category Filter (Flow Layout - Wrap)
                    FilterGroup(title = "Category", selectedCount = selectedCategoryIds.size) {
                        OutlinedTextField(
                            value = categoryQuery,
                            onValueChange = { categoryQuery = it },
                            placeholder = { Text("Search category...") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val filteredCategories = allCategories.filter { it.name.contains(categoryQuery, ignoreCase = true) }
                            filteredCategories.chunked(3).forEach { rowCats ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    rowCats.forEach { category ->
                                        val isSelected = category.id in selectedCategoryIds
                                        FilterChip(
                                            text = category.name,
                                            selected = isSelected,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                if (isSelected) selectedCategoryIds.remove(category.id)
                                                else selectedCategoryIds.add(category.id)
                                            }
                                        )
                                    }
                                    repeat(3 - rowCats.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (selectedPersonIds.isNotEmpty() || selectedCategoryIds.isNotEmpty() || selectedWalletIds.isNotEmpty() || selectedTagIds.isNotEmpty()) {
                TextButton(
                    onClick = {
                        selectedPersonIds.clear()
                        selectedCategoryIds.clear()
                        selectedWalletIds.clear()
                        selectedTagIds.clear()
                    },
                    modifier = Modifier.align(Alignment.End).padding(bottom = 8.dp)
                ) {
                    Text("Clear All Filters", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            when {
                transactions == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                filteredTransactions.isNullOrEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transactions found!", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { tx ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        txToDelete = tx.id
                                        false // Don't dismiss yet, wait for dialog
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val alignment = Alignment.CenterEnd
                                    // Using primaryContainer (FAB color) with 20% opacity
                                    val color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color, shape = RoundedCornerShape(16.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Text(
                                            "Delete", 
                                            color = MaterialTheme.colorScheme.onPrimaryContainer, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                TransactionItem(tx = tx)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isFullWidth = modifier == Modifier.fillMaxWidth()
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 4.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isFullWidth) Arrangement.Start else Arrangement.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).padding(end = 8.dp),
                    tint = Color.White
                )
            }
            Text(
                text = text,
                color = if (selected)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FilterGroup(
    title: String,
    selectedCount: Int,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selectedCount > 0) {
                Spacer(Modifier.width(8.dp))
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(selectedCount.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
        content()
    }
}

@Composable
fun TransactionItem(tx: TransactionUI) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min) // Important for the vertical accent to fill height
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT ACCENT
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        if (tx.type == "EXPENSE") getExpenseColor() else getIncomeColor()
                    )
            )

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE: Primary Info
                Column(modifier = Modifier.weight(1f)) {
                    if (!tx.person.isNullOrBlank()) {
                        Text(
                            text = tx.person,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (tx.type == "TRANSFER") {
                        Text(
                            text = "${tx.wallet} → ${tx.toWallet ?: "Unknown"}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "${tx.wallet} • ${tx.category}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (tx.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tx.tags.forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (!tx.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tx.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // RIGHT SIDE: Financial Info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (tx.type == "EXPENSE") "-" else "+"} ₹${tx.amount}",
                        color = if (tx.type == "EXPENSE") getExpenseColor() else getIncomeColor(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = tx.date,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = tx.time,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
