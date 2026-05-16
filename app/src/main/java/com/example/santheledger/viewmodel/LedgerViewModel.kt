package com.example.santheledger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.santheledger.data.AppDatabase
import com.example.santheledger.data.Customer
import com.example.santheledger.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    // Expose customer list as StateFlow so UI reacts to changes
    val customers = db.customerDao().getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expose transactions as StateFlow
    val transactions = db.transactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            db.customerDao().insertCustomer(Customer(name = name.trim()))
        }
    }

    fun addTransaction(customerId: Int, amount: Double, type: String) {
        viewModelScope.launch {
            db.transactionDao().insertTransaction(
                Transaction(customerId = customerId, amount = amount, type = type)
            )
        }
    }
}