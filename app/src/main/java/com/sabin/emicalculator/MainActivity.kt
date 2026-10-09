package com.sabin.emicalculator

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                EmiScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("emi_inputs", Context.MODE_PRIVATE) }
    val saved = remember {
        SavedInputs.parse(
            prefs.getString("principal", null), prefs.getString("rate", null),
            prefs.getString("tenure", null), prefs.getString("unit", null),
        )
    }
    var principal by remember { mutableStateOf(saved.principal) }
    var rate by remember { mutableStateOf(saved.rate) }
    var months by remember { mutableStateOf(saved.tenure) }
    var unit by remember { mutableStateOf(saved.unit) }
    LaunchedEffect(principal, rate, months, unit) {
        prefs.edit()
            .putString("principal", principal).putString("rate", rate)
            .putString("tenure", months).putString("unit", unit.name)
            .apply()
    }

    val loansPrefs = remember { context.getSharedPreferences("emi_loans", Context.MODE_PRIVATE) }
    var loans by remember { mutableStateOf(SavedLoans.deserialize(loansPrefs.getString("loans", null))) }
    var loanName by remember { mutableStateOf("") }
    fun updateLoans(new: List<SavedLoan>) {
        loans = new
        loansPrefs.edit().putString("loans", SavedLoans.serialize(new)).apply()
    }

    val p = principal.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val n = unit.parseToMonths(months)
    val result = runCatching { Emi.calculate(p!!, r!!, n!!) }.getOrNull()
    
    var prepay by remember { mutableStateOf("") }
    var prepayMonth by remember { mutableStateOf("12") }
    val prepayment = if (result != null) prepay.toDoubleOrNull()?.takeIf { it > 0 }?.let { amt ->
        runCatching { Emi.withPrepayment(p!!, r!!, n!!, amt, prepayMonth.toInt()) }.getOrNull()
    } else null

    val schedule = prepayment?.schedule ?: if (result != null) Emi.schedule(p!!, r!!, n!!) else emptyList()

    var compare by remember { mutableStateOf(false) }
    var principal2 by remember { mutableStateOf("500000") }
    var rate2 by remember { mutableStateOf("10") }
    var months2 by remember { mutableStateOf("60") }
    val p2 = principal2.toDoubleOrNull(); val r2 = rate2.toDoubleOrNull(); val n2 = unit.parseToMonths(months2)
    val comparison = if (compare) runCatching {
        LoanComparison.compare(LoanInput(p!!, r!!, n!!), LoanInput(p2!!, r2!!, n2!!))
    }.getOrNull() else null

    Scaffold(topBar = { TopAppBar(title = { Text("EMI Calculator") }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
            Column {
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
            OutlinedTextField(
                prepay, { prepay = it }, label = { Text("Prepayment (NPR, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                prepayMonth, { prepayMonth = it }, label = { Text("Prepayment after month #") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
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
                    Text("Enter valid positive values for both loans", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
            }
            OutlinedTextField(
                loanName, { loanName = it }, label = { Text("Loan name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Button(
                onClick = {
                    updateLoans(SavedLoans.add(loans, SavedLoan(loanName, SavedInputs(principal, rate, months, unit))))
                    loanName = ""
                },
                enabled = result != null && loanName.isNotBlank(),
            ) { Text("Save loan") }
            if (loans.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Saved loans", style = MaterialTheme.typography.titleMedium)
                loans.forEachIndexed { index, loan ->
                    val i = loan.inputs
                    val emi = runCatching {
                        Emi.calculate(i.principal.toDouble(), i.rate.toDouble(), i.unit.parseToMonths(i.tenure)!!).monthlyEmi
                    }.getOrNull()
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            principal = i.principal; rate = i.rate; months = i.tenure; unit = i.unit
                        }.padding(vertical = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(loan.name, fontWeight = FontWeight.Bold)
                            Text(if (emi != null) "EMI NPR %,.2f".format(emi) else "Invalid loan")
                        }
                        TextButton(onClick = { updateLoans(SavedLoans.removeAt(loans, index)) }) { Text("Delete") }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            if (result == null) {
                Text("Enter valid positive values", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Monthly EMI: NPR %,.2f".format(result.monthlyEmi), style = MaterialTheme.typography.titleLarge)
                        Text("Total interest: NPR %,.2f".format(result.totalInterest))
                        Text("Total payment: NPR %,.2f".format(result.totalPayment))
                    }
                }
                if (prepayment != null) {
                    Text("Interest saved: NPR %,.2f (%d months shorter)".format(prepayment.interestSaved, prepayment.monthsSaved),
                        style = MaterialTheme.typography.titleMedium)
                } else if (prepay.isNotBlank()) {
                    Text("Enter a valid prepayment and month within the tenure", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                Spacer(Modifier.height(12.dp))
                DonutChart(ChartData.shares(p!!, result.totalInterest))
                Spacer(Modifier.height(12.dp))
                BalanceLineChart(ChartData.yearlyBalance(p, schedule))
                Spacer(Modifier.height(12.dp))
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                ScheduleRowView(ScheduleTable.HEADER, bold = true)
                HorizontalDivider()
            }
            }
            }
            items(if (result == null) emptyList() else schedule) { row ->
                ScheduleRowView(ScheduleTable.cells(row), description = ScheduleTable.description(row))
            }
        }
    }
}

@Composable
private fun ScheduleRowView(cells: List<String>, bold: Boolean = false, description: String? = null) {
    val rowModifier = if (description != null) {
        Modifier.clearAndSetSemantics { contentDescription = description }
    } else Modifier
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).then(rowModifier)) {
        cells.forEachIndexed { i, text ->
            Text(
                text, Modifier.weight(if (i == 0) 0.7f else 1.2f), maxLines = 1, softWrap = false,
                textAlign = if (i == 0) TextAlign.Start else TextAlign.End,
                fontWeight = if (bold) FontWeight.Bold else null,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
