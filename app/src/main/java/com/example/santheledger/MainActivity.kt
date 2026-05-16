package com.example.santheledger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.santheledger.data.Customer
import com.example.santheledger.ui.theme.SantheLedgerTheme
import com.example.santheledger.viewmodel.LedgerViewModel
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private val viewModel: LedgerViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SantheLedgerTheme {
                HomeScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: LedgerViewModel) {

    val customers by viewModel.customers.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val startOfDay = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val todaySales = transactions
        .filter { it.type == "credit" && it.timestamp >= startOfDay }
        .sumOf { it.amount }

    val totalDue = transactions
        .filter { it.type == "credit" }.sumOf { it.amount } -
            transactions.filter { it.type == "payment" }.sumOf { it.amount }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Santhe Ledger", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCustomerDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── Daily Summary Card ────────────────────────────────
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "📊 Daily Summary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Dues pending ₹%.2f".format(totalDue),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (totalDue > 0)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total Customers: ${customers.size}",
                                    fontSize = 12.sp
                                )
                                Text(
                                    "Total Outstanding: ₹%.2f".format(totalDue),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalDue > 0)
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // ── Search Bar ────────────────────────────────────────
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search customer…") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true
                    )
                }

                // ── Customers Header ──────────────────────────────────
                item {
                    Text(
                        "Customers",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                // ── Empty State ───────────────────────────────────────
                if (filteredCustomers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (searchQuery.isBlank()) "No customers yet.\nTap + to add one."
                                else "No customer found for \"$searchQuery\"",
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ── Customer Cards ────────────────────────────────────
                items(filteredCustomers, key = { it.id }) { customer ->
                    val custTransactions =
                        transactions.filter { it.customerId == customer.id }
                    val due =
                        custTransactions.filter { it.type == "credit" }.sumOf { it.amount } -
                                custTransactions.filter { it.type == "payment" }.sumOf { it.amount }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    selectedCustomer = customer
                                    showAddTransactionDialog = true
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        customer.name,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${custTransactions.size} transaction(s)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    if (due > 0) "Owes ₹%.2f".format(due)
                                    else if (due < 0) "Advance ₹%.2f".format(-due)
                                    else "Settled",
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        due > 0 -> MaterialTheme.colorScheme.error
                                        due < 0 -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                )
                            }

                            if (due > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        val msg = "Namaskara ${customer.name}! " +
                                                "Nimma ₹%.2f udari iruttade. ".format(due) +
                                                "Dayavittu pay madiri 🙏"
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(
                                                "https://wa.me/?text=${Uri.encode(msg)}"
                                            )
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📲 Send WhatsApp Reminder")
                                }
                            }
                        }
                    }
                }

            } // end LazyColumn
        } // end Box
    } // end Scaffold

    // ── Add Customer Dialog ───────────────────────────────────────────────
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("Add Customer") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addCustomer(name)
                        showAddCustomerDialog = false
                    },
                    enabled = name.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Add Transaction Dialog ────────────────────────────────────────────
    if (showAddTransactionDialog && selectedCustomer != null) {
        var amount by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("credit") }

        AlertDialog(
            onDismissRequest = { showAddTransactionDialog = false },
            title = { Text("Add Transaction\n${selectedCustomer!!.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (amount.isEmpty()) "₹0" else "₹$amount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Enter Amount") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = type == "credit",
                            onClick = { type = "credit" },
                            label = { Text("Udari (Credit)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == "payment",
                            onClick = { type = "payment" },
                            label = { Text("Payment") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: return@TextButton
                        viewModel.addTransaction(selectedCustomer!!.id, amt, type)
                        showAddTransactionDialog = false
                        amount = ""
                    },
                    enabled = amount.toDoubleOrNull() != null
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTransactionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}