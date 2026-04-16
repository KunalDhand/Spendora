package com.example.testing.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object WalletNameProvider {
    private val _bankNames = MutableStateFlow<List<String>>(
        listOf(
            "State Bank of India", "Punjab National Bank", "Bank of Baroda", "Canara Bank",
            "Union Bank of India", "Indian Bank", "HDFC Bank", "ICICI Bank", "Axis Bank",
            "Kotak Mahindra Bank", "IndusInd Bank", "Bank of India", "Bank of Maharashtra",
            "Central Bank of India", "Indian Overseas Bank", "Punjab and Sind Bank",
            "UCO Bank", "Bandhan Bank", "CSB Bank", "City Union Bank", "DCB Bank",
            "Dhanlaxmi Bank", "Federal Bank", "IDBI Bank", "IDFC First Bank",
            "Jammu & Kashmir Bank", "Karnataka Bank", "Karur Vysya Bank", "Nainital Bank",
            "RBL Bank", "South Indian Bank", "YES Bank", "AU Small Finance Bank",
            "Capital Small Finance Bank", "Equitas Small Finance Bank",
            "ESAF Small Finance Bank", "Fincare Small Finance Bank",
            "Jana Small Finance Bank", "North East Small Finance Bank",
            "Suryoday Small Finance Bank", "Ujjivan Small Finance Bank",
            "Utkarsh Small Finance Bank"
        ).distinct().sorted()
    )
    val bankNames: StateFlow<List<String>> = _bankNames

    private val _upiProviders = MutableStateFlow<List<String>>(
        listOf(
            "Paytm", "Amazon Pay", "PhonePe Wallet", "MobiKwik", "Freecharge",
            "Airtel Money (Airtel Payments Bank Wallet)", "JioMoney", "PayZapp (HDFC)",
            "ICICI Pockets", "Ola Money", "Oxigen Wallet", "PayU Money",
            "Google Pay", "PhonePe", "Jupiter"
        ).distinct().sorted()
    )
    val upiProviders: StateFlow<List<String>> = _upiProviders

    private val _eRupeeProviders = MutableStateFlow<List<String>>(
        listOf(
            "eRupee by SBI", "Digital Rupee by ICICI Bank", "HDFC Bank Digital Rupee",
            "Axis Mobile Digital Rupee", "Digital Rupee by Kotak Bank",
            "Digital Rupee by IndusInd Bank", "Digital Rupee by UBI (Union Bank of India)",
            "Bank of Baroda Digital Rupee", "Canara Digital Rupee", "PNB Digital Rupee",
            "Federal Bank Digital Rupee", "Karnataka Bank Digital Rupee",
            "Indian Bank Digital Rupee", "IDFC FIRST Bank Digital Rupee",
            "YES Bank Digital Rupee", "IDBI eRupee", "Bank of India Digital Rupee",
            "eRupee by Bank of Maharashtra", "UCO Digital Rupee"
        ).distinct().sorted()
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
        // Remote fetching disabled for now. Using local list only.
    }
}
