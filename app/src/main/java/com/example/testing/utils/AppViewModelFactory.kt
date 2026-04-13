package com.example.testing.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.testing.data.local.AppDatabase
import com.example.testing.data.repository.CategoryRepository
import com.example.testing.data.repository.TagRepository
import com.example.testing.data.repository.TransactionRepository
import com.example.testing.data.repository.WalletRepository
import com.example.testing.ui.viewmodel.CategoryViewModel
import com.example.testing.ui.viewmodel.PersonViewModel
import com.example.testing.ui.viewmodel.TagViewModel
import com.example.testing.ui.viewmodel.TransactionViewModel
import com.example.testing.ui.viewmodel.WalletViewModel

class AppViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TransactionViewModel(
                    TransactionRepository(
                        db.transactionDao(),
                        db.categoryDao(),
                        db.walletDao(),
                        db.tagDao(),
                        db.personDao()
                    )
                ) as T
            }
            modelClass.isAssignableFrom(WalletViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                WalletViewModel(
                    WalletRepository(
                        db.walletDao(),
                        db.transactionDao()
                    )
                ) as T
            }
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CategoryViewModel(
                    CategoryRepository(db.categoryDao())
                ) as T
            }
            modelClass.isAssignableFrom(PersonViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PersonViewModel(db.personDao()) as T
            }
            modelClass.isAssignableFrom(TagViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TagViewModel(
                    TagRepository(db.tagDao())
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
