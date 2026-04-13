package com.example.testing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
//import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.testing.ui.theme.getExpenseColor
import com.example.testing.ui.theme.getIncomeColor
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.MoreVert
import com.example.testing.data.local.WalletEntity
import com.example.testing.ui.viewmodel.TransactionViewModel
import com.example.testing.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    txViewModel: TransactionViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallets by walletViewModel.wallets.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var walletToDelete by remember { mutableStateOf<WalletEntity?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        walletViewModel.error.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showAddDialog) {
        AddWalletDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name: String, balance: Double? ->
                walletViewModel.addWallet(name, balance)
            }
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            wallets = wallets,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromId, toId, amount ->
                walletViewModel.transferMoney(fromId, toId, amount)
            }
        )
    }

    if (walletToDelete != null) {
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            title = { Text("Delete Wallet") },
            text = { Text("Are you sure you want to delete ${walletToDelete?.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val wallet = walletToDelete
                        if (wallet != null) {
                            walletViewModel.deleteWallet(wallet) { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }
                        walletToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = getExpenseColor())
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Wallets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showTransferDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("+ Add", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wallets) { wallet ->
                WalletItem(
                    wallet = wallet,
                    onDelete = { walletToDelete = wallet }
                )
            }

            if (wallets.size == 1 && wallets.firstOrNull()?.isDefault == true) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💡 Add more wallets to better manage your money.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Track expenses across Bank, UPI, Crypto and more for smarter insights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(
                                onClick = { showAddDialog = true }
                            ) {
                                Text(
                                    text = "+ Add Wallet",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double?) -> Unit
) {
    var walletName by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var step2 by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Wallet", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    label = { Text("Wallet Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )

                AnimatedVisibility(visible = step2) {
                    Column {
                        OutlinedTextField(
                            value = openingBalance,
                            onValueChange = { openingBalance = it },
                            label = { Text("Opening Balance (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!step2) {
                        if (walletName.isNotBlank()) {
                            step2 = true
                        }
                    } else {
                        val balance = openingBalance.toDoubleOrNull()
                        if (walletName.isNotBlank()) {
                            onAdd(walletName, balance)
                            onDismiss()
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (step2) "Add" else "Next", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    wallets: List<WalletEntity>,
    onDismiss: () -> Unit,
    onTransfer: (Int, Int, Double) -> Unit
) {
    var fromWallet by remember { mutableStateOf<WalletEntity?>(null) }
    var toWallet by remember { mutableStateOf<WalletEntity?>(null) }
    var amountText by remember { mutableStateOf("") }
    
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Money", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // From Wallet
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fromWallet?.name ?: "Select Source",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        wallets.forEach { wallet ->
                            DropdownMenuItem(
                                text = { Text("${wallet.name} (₹${"%.2f".format(wallet.balance)})") },
                                onClick = {
                                    fromWallet = wallet
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                // To Wallet
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = it }
                ) {
                    OutlinedTextField(
                        value = toWallet?.name ?: "Select Destination",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        wallets.forEach { wallet ->
                            DropdownMenuItem(
                                text = { Text("${wallet.name} (₹${"%.2f".format(wallet.balance)})") },
                                onClick = {
                                    toWallet = wallet
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (fromWallet != null && toWallet != null && amount != null && amount > 0 && fromWallet?.id != toWallet?.id) {
                        onTransfer(fromWallet!!.id, toWallet!!.id, amount)
                        onDismiss()
                    }
                },
                enabled = fromWallet != null && toWallet != null && amountText.isNotBlank() && fromWallet?.id != toWallet?.id,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Transfer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun WalletItem(
    wallet: WalletEntity,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "₹${"%.2f".format(wallet.balance)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (wallet.balance >= 0) getIncomeColor() else getExpenseColor(),
                    fontWeight = FontWeight.Bold
                )
            }

            if (!wallet.isDefault) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Wallet Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Wallet", color = getExpenseColor()) },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
