package com.example.testing.domain.model

import androidx.compose.runtime.*
import com.example.testing.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class WalletType(val label: String, val selectable: Boolean) {
    BANK("Bank Account", true),
    UPI("UPI Wallet", true),
    ERUPEE("eRupee", true),
    CRYPTO("Crypto", true),
    OTHER("Other", true)
}

object WalletNameProvider {
    private val _bankNames = MutableStateFlow<List<String>>(emptyList())
    val bankNames = _bankNames.asStateFlow()

    private val _upiProviders = MutableStateFlow<List<String>>(emptyList())
    val upiProviders = _upiProviders.asStateFlow()

    private val staticBanks = listOf(
        "State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank",
        "IndusInd Bank", "Bank of Baroda", "Punjab National Bank", "Canara Bank", "Union Bank of India",
        "IDBI Bank", "Yes Bank", "IDFC First Bank", "Federal Bank", "South Indian Bank", "UCO Bank",
        "Indian Bank", "Central Bank of India", "Bank of India", "Bank of Maharashtra"
    ).sorted()

    private val staticUpi = listOf(
        "PhonePe", "Google Pay", "Paytm", "Amazon Pay", "BHIM UPI", "MobiKwik", "Freecharge", "Airtel Money"
    ).sorted()

    private val staticErupee = listOf(
        "SBI eRupee", "HDFC eRupee", "ICICI eRupee", "Axis eRupee", "IDFC First eRupee", "Canara eRupee"
    ).sorted()

    private val staticCrypto = listOf(
        "Binance", "Coinbase", "Kraken", "WazirX", "CoinDCX", "MetaMask", "Trust Wallet"
    ).sorted()

    fun fetchRemoteNames(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            try {
                _bankNames.value = NetworkModule.api.getBankNames()
            } catch (e: Exception) {
                _bankNames.value = staticBanks
            }
            try {
                _upiProviders.value = NetworkModule.api.getUpiProviders()
            } catch (e: Exception) {
                _upiProviders.value = staticUpi
            }
        }
    }

    fun getNames(type: WalletType): List<String> {
        return when (type) {
            WalletType.BANK -> _bankNames.value.ifEmpty { staticBanks }
            WalletType.UPI -> _upiProviders.value.ifEmpty { staticUpi }
            WalletType.ERUPEE -> staticErupee
            WalletType.CRYPTO -> staticCrypto
            else -> emptyList()
        }
    }
}

