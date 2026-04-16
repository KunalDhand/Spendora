package com.example.testing.domain.model

import com.example.testing.data.remote.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object WalletNameProvider {
    private val _bankNames = MutableStateFlow<List<String>>(
        listOf("HDFC Bank", "SBI", "ICICI Bank", "Axis Bank", "Kotak Mahindra", "Punjab National Bank")
    )
    val bankNames: StateFlow<List<String>> = _bankNames

    private val _upiProviders = MutableStateFlow<List<String>>(
        listOf("Google Pay", "PhonePe", "Paytm", "Amazon Pay", "Mobikwik", "Jupiter")
    )
    val upiProviders: StateFlow<List<String>> = _upiProviders

    private val _eRupeeProviders = MutableStateFlow<List<String>>(
        listOf("RBI eRupee", "SBI eRupee", "ICICI eRupee", "HDFC eRupee")
    )
    val eRupeeProviders: StateFlow<List<String>> = _eRupeeProviders

    fun getNames(type: WalletType): List<String> {
        return when (type) {
            WalletType.BANK -> bankNames.value
            WalletType.UPI -> upiProviders.value
            WalletType.ERUPEE -> eRupeeProviders.value
            else -> emptyList()
        }
    }

    fun fetchRemoteNames(scope: CoroutineScope) {
        scope.launch {
            try {
                val remoteBanks = NetworkModule.api.getBankNames()
                if (remoteBanks.isNotEmpty()) _bankNames.value = remoteBanks
            } catch (e: Exception) {
                // Fallback to defaults
            }

            try {
                val remoteUpi = NetworkModule.api.getUpiProviders()
                if (remoteUpi.isNotEmpty()) _upiProviders.value = remoteUpi
            } catch (e: Exception) {
                // Fallback to defaults
            }
        }
    }
}
