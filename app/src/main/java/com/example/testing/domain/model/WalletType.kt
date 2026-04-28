package com.example.testing.domain.model

enum class WalletType(val label: String, val selectable: Boolean) {
    BANK("Bank Account", true),
    UPI("UPI Wallet", true),
    ERUPEE("eRupee", true),
    CRYPTO("Crypto (Coming Soon)", false),
    OTHER("Other", true)
}