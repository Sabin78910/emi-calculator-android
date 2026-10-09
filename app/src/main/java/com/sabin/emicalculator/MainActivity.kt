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
    var unit by remember { mutableStateOf(TenureUnit.MONTHS) }

    val p = principal.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val n = unit.parseToMonths(months)
    val result = runCatching { Emi.calculate(p!!, r!!, n!!) }.getOrNull()
    val schedule = if (result != null) Emi.schedule(p!!, r!!, n!!) else emptyList()

    var compare by remember { mutableStateOf(false) }
    var principal2 by remember { mutableStateOf("500000") }
    var rate2 by remember { mutableStateOf("10") }
    var months2 by remember { mutableStateOf("60") }
    val p2 = principal2.toDoubleOrNull(); val r2 = rate2.toDoubleOrNull(); val n2 = unit.parseToMonths(months2)
    val comparison = if (compare) runCatching {
        LoanComparison.compare(LoanInput(p!!, r!!, n!!), LoanInput(p2!!, r2!!, n2!!))
    }.getOrNull() else null

    Scaffold(topBar = { TopAppBar(title = { Text("EMI Calculator") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            listOf(
                Triple("Loan amount (NPR)", principal) { v: String -> principal = v },
                Triple("Interest rate (% per year)", rate) { v: String -> rate = v },
                Triple("Tenure (${unit.label.lowercase()})", months) { v: String -> months = v },
            ).forEach { (label, value, onChange) ->
                OutlinedTextField(
                    value, onChange, label = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TenureUnit.values().forEach { u ->
                    FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u.label) })
                }
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = compare, onCheckedChange = { compare = it })
                Spacer(Modifier.width(8.dp))
                Text("Compare with second loan")
            }
            if (compare) {
                listOf(
                    Triple("Loan 2 amount (NPR)", principal2) { v: String -> principal2 = v },
                    Triple("Loan 2 rate (% per year)", rate2) { v: String -> rate2 = v },
                    Triple("Loan 2 tenure (${unit.label.lowercase()})", months2) { v: String -> months2 = v },
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value, onChange, label = { Text(label) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                if (comparison != null) {
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Loan 1: EMI NPR %,.2f, interest NPR %,.2f".format(comparison.a.monthlyEmi, comparison.a.totalInterest))
                            Text("Loan 2: EMI NPR %,.2f, interest NPR %,.2f".format(comparison.b.monthlyEmi, comparison.b.totalInterest))
                            Text("Difference (2 − 1): EMI NPR %,.2f, interest NPR %,.2f".format(comparison.emiDifference, comparison.interestDifference),
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    Text("Enter valid positive values for both loans", color = MaterialTheme.colorScheme.error)
                }
            }
            if (result == null) {
                Text("Enter valid positive values", color = MaterialTheme.colorScheme.error)
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Monthly EMI: NPR %,.2f".format(result.monthlyEmi), style = MaterialTheme.typography.titleLarge)
                        Text("Total interest: NPR %,.2f".format(result.totalInterest))
                        Text("Total payment: NPR %,.2f".format(result.totalPayment))
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
