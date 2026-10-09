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
    var prepay by remember { mutableStateOf("") }
    var prepayMonth by remember { mutableStateOf("12") }
    var unit by remember { mutableStateOf(TenureUnit.MONTHS) }

    val p = principal.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val n = unit.parseToMonths(months)
    val result = runCatching { Emi.calculate(p!!, r!!, n!!) }.getOrNull()
    val schedule = if (result != null) Emi.schedule(p!!, r!!, n!!) else emptyList()

    val pm = prepayMonth.toIntOrNull()
    val prepayAmount = if (prepay.isBlank()) 0.0 else prepay.toDoubleOrNull()
    val prepayResult = if (result != null && prepayAmount != null && prepayAmount > 0 && pm != null)
        runCatching { Emi.scheduleWithPrepayment(p!!, r!!, n!!, prepayAmount, pm) }.getOrNull() else null

    Scaffold(topBar = { TopAppBar(title = { Text("EMI Calculator") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            listOf(
                Triple("Loan amount (NPR)", principal) { v: String -> principal = v },
                Triple("Interest rate (% per year)", rate) { v: String -> rate = v },
                Triple("Tenure (${unit.label.lowercase()})", months) { v: String -> months = v },
                Triple("Prepayment (NPR, optional)", prepay) { v: String -> prepay = v },
                Triple("Prepay after month", prepayMonth) { v: String -> prepayMonth = v },
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
            if (result == null) {
                Text("Enter valid positive values", color = MaterialTheme.colorScheme.error)
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Monthly EMI: NPR %,.2f".format(result.monthlyEmi), style = MaterialTheme.typography.titleLarge)
                        Text("Total interest: NPR %,.2f".format(result.totalInterest))
                        Text("Total payment: NPR %,.2f".format(result.totalPayment))
                        if (prepayResult != null) {
                            Text("Interest saved: NPR %,.2f".format(prepayResult.interestSaved))
                            Text("Months saved: %d".format(prepayResult.monthsSaved))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(prepayResult?.schedule ?: schedule) { row ->
                        Text("#%d  principal %,.0f  interest %,.0f  balance %,.0f"
                            .format(row.month, row.principal, row.interest, row.balance))
                    }
                }
            }
        }
    }
}
