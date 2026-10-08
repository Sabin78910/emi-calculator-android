package com.sabin.emicalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { EmiScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiScreen() {
    var principal by remember { mutableStateOf("500000") }
    var rate by remember { mutableStateOf("12") }
    var months by remember { mutableStateOf("60") }
    var prepay by remember { mutableStateOf("0") }
    var prepayMonth by remember { mutableStateOf("12") }

    val p = principal.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val n = months.toIntOrNull()
    val result = runCatching { Emi.calculate(p!!, r!!, n!!) }.getOrNull()
    val schedule = if (result != null) Emi.schedule(p!!, r!!, n!!) else emptyList()
    val pre = prepay.toDoubleOrNull(); val preMonth = prepayMonth.toIntOrNull()
    val saved = if (result != null && pre != null && pre > 0 && preMonth != null)
        runCatching {
            Emi.interestSaved(p!!, r!!, n!!, pre, preMonth) to
                Emi.scheduleWithPrepayment(p, r, n, pre, preMonth).size
        }.getOrNull() else null

    Scaffold(topBar = { TopAppBar(title = { Text("EMI Calculator") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            listOf(
                Triple("Loan amount (NPR)", principal) { v: String -> principal = v },
                Triple("Interest rate (% per year)", rate) { v: String -> rate = v },
                Triple("Tenure (months)", months) { v: String -> months = v },
                Triple("Prepayment (NPR, optional)", prepay) { v: String -> prepay = v },
                Triple("Prepay after month", prepayMonth) { v: String -> prepayMonth = v },
            ).forEach { (label, value, onChange) ->
                OutlinedTextField(
                    value, onChange, label = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            if (result == null) {
                Text("Enter valid positive values", color = MaterialTheme.colorScheme.error)
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Monthly EMI: NPR %,.2f".format(result.monthlyEmi), style = MaterialTheme.typography.titleLarge)
                        Text("Total interest: NPR %,.2f".format(result.totalInterest))
                        Text("Total payment: NPR %,.2f".format(result.totalPayment))
                        if (saved != null) {
                            Text("Interest saved: NPR %,.2f (loan ends in %d months)".format(saved.first, saved.second))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(schedule) { row ->
                        Text("#%d  principal %,.0f  interest %,.0f  balance %,.0f"
                            .format(row.month, row.principal, row.interest, row.balance))
                    }
                }
            }
        }
    }
}
