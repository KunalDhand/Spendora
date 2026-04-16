package com.example.testing.data.remote

import retrofit2.http.GET

interface WalletApi {
    @GET("bank_names.json")
    suspend fun getBankNames(): List<String>

    @GET("upi_providers.json")
    suspend fun getUpiProviders(): List<String>
}
