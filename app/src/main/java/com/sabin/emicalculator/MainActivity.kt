package com.sabin.emicalculator

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
            EmiTheme {
                val prefs = remember { getSharedPreferences(Onboarding.PREFS, Context.MODE_PRIVATE) }
                var onboarding by remember {
                    mutableStateOf(Onboarding.shouldShow(prefs.getBoolean(Onboarding.KEY_DONE, false)))
                }
                if (onboarding) {
                    OnboardingScreen { onboarding = false }
                } else {
                    EmiScreen()
                }
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
    val scope = rememberCoroutineScope()
    var loans by remember { mutableStateOf(SavedLoans.deserialize(loansPrefs.getString("loans", null))) }
    var loanName by remember { mutableStateOf("") }
    fun updateLoans(new: List<SavedLoan>) {
        loans = new
        loansPrefs.edit().putString(EmiWidget.LOANS_KEY, SavedLoans.serialize(new)).apply()
        scope.launch { EmiWidget.refresh(context) }
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeTopAppBar(title = { Text("EMI Calculator") }, scrollBehavior = scrollBehavior) },
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp)) {
            item {
            Column {
            SliderField("Loan amount (NPR)", principal, SliderRange.AMOUNT, 0) { principal = it }
            SliderField("Interest rate (% per year)", rate, SliderRange.RATE, 1) { rate = it }
            SliderField(
                "Tenure (${unit.label.lowercase()})", months,
                if (unit == TenureUnit.YEARS) SliderRange(1f, 30f) else SliderRange.TENURE, 0,
            ) { months = it }
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
                shape = CircleShape,
                onClick = {
                    updateLoans(SavedLoans.add(loans, SavedLoan(loanName, SavedInputs(principal, rate, months, unit))))
                    loanName = ""
                },
                enabled = result != null && loanName.isNotBlank(),
            ) { Text("Save loan") }
            Spacer(Modifier.height(12.dp))
            Text("Saved loans", style = MaterialTheme.typography.titleMedium)
            if (loans.isEmpty()) {
                EmptyLoans()
            } else {
                loans.forEachIndexed { index, loan ->
                    val i = loan.inputs
                    val emi = runCatching {
                        Emi.calculate(i.principal.toDouble(), i.rate.toDouble(), i.unit.parseToMonths(i.tenure)!!).monthlyEmi
                    }.getOrNull()
                    ElevatedCard(
                        onClick = { principal = i.principal; rate = i.rate; months = i.tenure; unit = i.unit },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(loan.name, fontWeight = FontWeight.Bold)
                                Text(if (emi != null) "EMI " + HeroFormat.money(emi) else "Invalid loan")
                                PayoffSection(loan, onMarkPaid = { updateLoans(SavedLoans.markPaid(loans, index)) })
                            }
                            TextButton(onClick = { updateLoans(SavedLoans.removeAt(loans, index)) }) { Text("Delete") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (result == null) {
                Text("Enter valid positive values", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            } else {
                HeroCard(result, ChartData.shares(p!!, result.totalInterest))
                TextButton(onClick = {
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, LoanSummary.text(result, n!!))
                    }
                    context.startActivity(android.content.Intent.createChooser(send, "Share loan summary"))
                }) { Text("Share") }
                if (prepayment != null) {
                    SavingsCard(prepayment.interestSaved, prepayment.monthsSaved)
                } else if (prepay.isNotBlank()) {
                    Text("Enter a valid prepayment and month within the tenure", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
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
private fun animationsEnabled(): Boolean = android.provider.Settings.Global.getFloat(
    LocalContext.current.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f

@Composable
private fun HeroCard(result: EmiResult, shares: Shares) {
    val on = animationsEnabled()
    val spring = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    val emi = remember { Animatable(if (on) 0f else result.monthlyEmi.toFloat()) }
    val sweep = remember { Animatable(if (on) 0f else shares.principalPercent.toFloat()) }
    LaunchedEffect(result.monthlyEmi) { if (on) emi.animateTo(result.monthlyEmi.toFloat(), spring) else emi.snapTo(result.monthlyEmi.toFloat()) }
    LaunchedEffect(shares) { if (on) sweep.animateTo(shares.principalPercent.toFloat(), spring) else sweep.snapTo(shares.principalPercent.toFloat()) }
    val principalColor = MaterialTheme.colorScheme.primary
    val interestColor = MaterialTheme.colorScheme.tertiary
    ElevatedCard(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = HeroFormat.description(result) + ". " + ChartData.donutDescription(shares)
            liveRegion = LiveRegionMode.Polite
        },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text("Monthly EMI", style = MaterialTheme.typography.labelLarge)
            Text(HeroFormat.money(emi.value.toDouble()), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Canvas(Modifier.size(160.dp)) {
                val stroke = 24.dp.toPx()
                val arc = Size(size.width - stroke, size.height - stroke)
                val top = Offset(stroke / 2, stroke / 2)
                val p = sweep.value / 100f * 360f
                drawArc(principalColor, -90f, p, false, top, arc, style = Stroke(stroke))
                drawArc(interestColor, -90f + p, 360f - p, false, top, arc, style = Stroke(stroke))
            }
            Text(HeroFormat.legend(shares), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total interest", style = MaterialTheme.typography.labelMedium)
                    Text(HeroFormat.money(result.totalInterest), fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text("Total payable", style = MaterialTheme.typography.labelMedium)
                    Text(HeroFormat.money(result.totalPayment), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyLoans() {
    val color = MaterialTheme.colorScheme.outline
    Column(
        Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(72.dp).semantics { contentDescription = "Empty wallet illustration" }) {
            val w = 6.dp.toPx()
            drawRoundRect(color, Offset(w, size.height * 0.25f), Size(size.width - 2 * w, size.height * 0.6f),
                androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()), style = Stroke(w))
            drawLine(color, Offset(size.width * 0.6f, size.height * 0.55f), Offset(size.width * 0.8f, size.height * 0.55f),
                w, StrokeCap.Round)
        }
        Text("No saved loans yet", style = MaterialTheme.typography.titleSmall)
        Text("Name your loan above and tap Save loan", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SavingsCard(interestSaved: Double, monthsSaved: Int) {
    val context = LocalContext.current
    val animationsOn = android.provider.Settings.Global.getFloat(
        context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    val progress = remember { Animatable(if (animationsOn) 0f else 1f) }
    LaunchedEffect(interestSaved, monthsSaved) {
        if (animationsOn) { progress.snapTo(0f); progress.animateTo(1f, tween(1000)) }
    }
    Card(Modifier.fillMaxWidth().semantics {
        contentDescription = PrepaymentSavings.message(interestSaved, monthsSaved, 1f)
        liveRegion = LiveRegionMode.Polite
    }) {
        Text(PrepaymentSavings.message(interestSaved, monthsSaved, progress.value),
            Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
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

@Composable
internal fun SliderField(
    label: String, value: String, range: SliderRange, decimals: Int, onChange: (String) -> Unit,
) {
    Column(Modifier.padding(bottom = 8.dp)) {
        OutlinedTextField(
            value, onChange, label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            value = range.position(value),
            onValueChange = { onChange(range.text(it, decimals)) },
            valueRange = range.min..range.max,
            modifier = Modifier.semantics { contentDescription = "$label slider" },
        )
    }
}

@Composable
private fun PayoffSection(loan: SavedLoan, onMarkPaid: () -> Unit) {
    val progress = PayoffProgress.of(loan.inputs, loan.paidMonths) ?: return
    val percent = (progress.fractionPaid * 100).toInt()
    val done = progress.monthsLeft == 0
    var celebrate by remember { mutableStateOf<Int?>(null) }
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        val ringColor = MaterialTheme.colorScheme.primary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(Modifier.size(48.dp).padding(4.dp).semantics { contentDescription = "$percent% of principal paid" }) {
            val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            drawArc(trackColor, 0f, 360f, false, style = stroke)
            drawArc(ringColor, -90f, (progress.fractionPaid * 360).toFloat(), false, style = stroke)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text("$percent% paid · ${progress.paidMonths}/${progress.totalMonths} months")
            Text(
                if (done) "Debt free! \uD83C\uDF89"
                else "${progress.monthsLeft} months left · debt-free " +
                    PayoffProgress.debtFreeDate(java.time.LocalDate.now(), progress.monthsLeft)
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    val badges = PayoffProgress.milestones(progress.fractionPaid)
    if (badges.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            badges.forEach { AssistChip(onClick = {}, label = { Text("$it%") }) }
        }
    }
    celebrate?.let { m ->
        Text(
            if (m == 100) "Congratulations, loan fully paid! \uD83C\uDF89" else "Milestone reached: $m% paid! \uD83C\uDF8A",
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    TextButton(
        enabled = !done,
        onClick = {
            val next = PayoffProgress.of(loan.inputs, PayoffProgress.markPaid(loan.inputs, loan.paidMonths))
            celebrate = next?.let { PayoffProgress.newMilestone(progress.fractionPaid, it.fractionPaid) }
            onMarkPaid()
        },
    ) { Text("Mark this month paid") }
}
