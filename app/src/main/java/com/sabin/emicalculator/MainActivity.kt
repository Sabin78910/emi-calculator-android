package com.sabin.emicalculator

import androidx.compose.ui.res.stringResource
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
        ReviewRequester(this).recordLaunch()
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
            prefs.getString("tenure", null), prefs.getString("unit", null), prefs.getString("fee", null),
        )
    }
    var principal by remember { mutableStateOf(saved.principal) }
    var rate by remember { mutableStateOf(saved.rate) }
    var months by remember { mutableStateOf(saved.tenure) }
    var unit by remember { mutableStateOf(saved.unit) }
    var fee by remember { mutableStateOf(saved.fee) }
    LaunchedEffect(principal, rate, months, unit, fee) {
        prefs.edit()
            .putString("principal", principal).putString("rate", rate)
            .putString("tenure", months).putString("unit", unit.name).putString("fee", fee)
            .apply()
    }

    val reviews = remember { ReviewRequester(context) }
    val loansPrefs = remember { context.getSharedPreferences("emi_loans", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var loans by remember { mutableStateOf(SavedLoans.deserialize(loansPrefs.getString("loans", null))) }
    var loanName by remember { mutableStateOf("") }
    fun updateLoans(new: List<SavedLoan>) {
        loans = new
        loansPrefs.edit().putString(EmiWidget.LOANS_KEY, SavedLoans.serialize(new)).apply()
        scope.launch { EmiWidget.refresh(context) }
        ReminderAlarm.reschedule(context)
    }

    val p = principal.toDoubleOrNull(); val r = rate.toDoubleOrNull(); val n = unit.parseToMonths(months)
    val result = runCatching { Emi.calculate(p!!, r!!, n!!) }.getOrNull()
    
    val feePercent = ProcessingFee.parse(fee)
    val feeAmount = if (result != null && feePercent != null) ProcessingFee.amount(p!!, feePercent) else 0.0
    val effectiveRate = if (result != null && feePercent != null && feePercent > 0)
        ProcessingFee.effectiveRate(p!!, result.monthlyEmi, n!!, feePercent) else null

    var prepay by remember { mutableStateOf("") }
    var prepayMonth by remember { mutableStateOf("12") }
    val prepayment = if (result != null) prepay.toDoubleOrNull()?.takeIf { it > 0 }?.let { amt ->
        runCatching { Emi.withPrepayment(p!!, r!!, n!!, amt, prepayMonth.toInt()) }.getOrNull()
    } else null

    val changeMonthLabel = stringResource(R.string.rate_change_month)
    val changeRateLabel = stringResource(R.string.rate_change_rate)
    var changeMonth by remember { mutableStateOf("") }
    var changeRate by remember { mutableStateOf("") }
    val rateChange = if (result != null) changeRate.toDoubleOrNull()?.let { nr ->
        runCatching { RateChange.apply(p!!, r!!, n!!, changeMonth.toInt(), nr) }.getOrNull()
    } else null

    val moratoriumLabel = stringResource(R.string.moratorium_months)
    var moratorium by remember { mutableStateOf("") }
    val moratoriumResult = if (result != null) moratorium.toIntOrNull()?.takeIf { it > 0 }?.let { k ->
        runCatching { Moratorium.apply(p!!, r!!, n!!, k) }.getOrNull()
    } else null

    val flatLabel = stringResource(R.string.flat_rate_input)
    var flatRate by remember { mutableStateOf("") }
    val flatRateValue = flatRate.toDoubleOrNull()
    val flatEquivalent = if (result != null && flatRateValue != null) runCatching {
        Triple(FlatRate.flatEmi(p!!, flatRateValue, n!!), FlatRate.reducingRateFromFlat(p, flatRateValue, n), FlatRate.flatFromReducing(p, r!!, n))
    }.getOrNull() else null

    val impliedEmiLabel = stringResource(R.string.implied_emi_input)
    var impliedEmi by remember { mutableStateOf("") }
    val impliedEmiValue = impliedEmi.toDoubleOrNull()
    val impliedRate = if (impliedEmiValue != null && p != null && n != null) runCatching {
        ImpliedRate.annualRatePercent(p, impliedEmiValue, n)
    }.getOrNull() else null

    val schedule = prepayment?.schedule ?: rateChange?.schedule ?: moratoriumResult?.schedule ?: if (result != null) Emi.schedule(p!!, r!!, n!!) else emptyList()

    var compare by remember { mutableStateOf(false) }
    var principal2 by remember { mutableStateOf("500000") }
    var rate2 by remember { mutableStateOf("10") }
    var months2 by remember { mutableStateOf("60") }
    val p2 = principal2.toDoubleOrNull(); val r2 = rate2.toDoubleOrNull(); val n2 = unit.parseToMonths(months2)
    val comparison = if (compare) runCatching {
        LoanComparison.compare(LoanInput(p!!, r!!, n!!), LoanInput(p2!!, r2!!, n2!!))
    }.getOrNull() else null

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }, scrollBehavior = scrollBehavior) },
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp)) {
            item {
            Column {
            if (result == null) {
                Text(stringResource(R.string.invalid_values), color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            } else {
                HeroCard(result, ChartData.shares(p!!, result.totalInterest))
                val shareChooser = stringResource(R.string.share_chooser)
                TextButton(onClick = {
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, LoanSummary.text(result, n!!))
                    }
                    context.startActivity(android.content.Intent.createChooser(send, shareChooser))
                }) { Text(stringResource(R.string.share)) }
            }
            Spacer(Modifier.height(8.dp))
            SliderField(stringResource(R.string.loan_amount), principal, SliderRange.AMOUNT, 0) { principal = it }
            SliderField(stringResource(R.string.interest_rate), rate, SliderRange.RATE, 1) { rate = it }
            SliderField(
                stringResource(if (unit == TenureUnit.YEARS) R.string.tenure_years else R.string.tenure_months), months,
                unit.sliderRange, 0,
            ) { months = it }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                TenureUnit.values().forEachIndexed { index, u ->
                    SegmentedButton(
                        selected = unit == u,
                        onClick = {
                            months = TenureUnit.convertText(months, unit, u)
                            months2 = TenureUnit.convertText(months2, unit, u)
                            unit = u
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, TenureUnit.values().size),
                    ) { Text(stringResource(if (u == TenureUnit.YEARS) R.string.unit_years else R.string.unit_months)) }
                }
            }
            OutlinedTextField(
                fee, { fee = NumericInput.filter(it, true) }, label = { Text(stringResource(R.string.processing_fee)) },
                isError = feePercent == null,
                supportingText = if (feePercent == null) ({ Text(stringResource(R.string.invalid_fee)) }) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                prepay, { prepay = NumericInput.filter(it, true) }, label = { Text(stringResource(R.string.prepayment_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                prepayMonth, { prepayMonth = NumericInput.filter(it, false) }, label = { Text(stringResource(R.string.prepayment_month)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                changeMonth, { changeMonth = NumericInput.filter(it, false) }, label = { Text(stringResource(R.string.rate_change_month)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics { contentDescription = changeMonthLabel }
            )
            OutlinedTextField(
                changeRate, { changeRate = NumericInput.filter(it, true) }, label = { Text(stringResource(R.string.rate_change_rate)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics { contentDescription = changeRateLabel }
            )
            OutlinedTextField(
                moratorium, { moratorium = NumericInput.filter(it, false) }, label = { Text(stringResource(R.string.moratorium_months)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics { contentDescription = moratoriumLabel }
            )
            OutlinedTextField(
                flatRate, { flatRate = NumericInput.filter(it, true) }, label = { Text(stringResource(R.string.flat_rate_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics { contentDescription = flatLabel }
            )
            OutlinedTextField(
                impliedEmi, { impliedEmi = NumericInput.filter(it, true) }, label = { Text(stringResource(R.string.implied_emi_input)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics { contentDescription = impliedEmiLabel }
            )
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(stringResource(R.string.compare_second), Modifier.weight(1f))
                    Switch(checked = compare, onCheckedChange = { compare = it })
                }
            }
            if (compare) {
                listOf(
                    Triple(stringResource(R.string.loan2_amount), principal2) { v: String -> principal2 = v },
                    Triple(stringResource(R.string.loan2_rate), rate2) { v: String -> rate2 = v },
                    Triple(stringResource(if (unit == TenureUnit.YEARS) R.string.loan2_tenure_years else R.string.loan2_tenure_months), months2) { v: String -> months2 = v },
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value, { onChange(NumericInput.filter(it, true)) }, label = { Text(label) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, shape = FIELD_SHAPE,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
                if (comparison != null) {
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.compare_loan1, "%,.2f".format(comparison.a.monthlyEmi), "%,.2f".format(comparison.a.totalInterest)))
                            Text(stringResource(R.string.compare_loan2, "%,.2f".format(comparison.b.monthlyEmi), "%,.2f".format(comparison.b.totalInterest)))
                            Text(stringResource(R.string.compare_diff, "%,.2f".format(comparison.emiDifference), "%,.2f".format(comparison.interestDifference)),
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    Text(stringResource(R.string.invalid_both), color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
            }
            OutlinedTextField(
                loanName, { loanName = it }, label = { Text(stringResource(R.string.loan_name)) }, singleLine = true, shape = FIELD_SHAPE,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Button(
                shape = CircleShape,
                onClick = {
                    updateLoans(SavedLoans.add(loans, SavedLoan(loanName, SavedInputs(principal, rate, months, unit, fee))))
                    (context as? android.app.Activity)?.let { reviews.maybeAsk(it, HappyMoment.LOAN_SAVED, hadError = result == null) }
                    loanName = ""
                },
                enabled = result != null && loanName.isNotBlank(),
            ) { Text(stringResource(R.string.save_loan)) }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.saved_loans), style = MaterialTheme.typography.titleMedium)
            if (loans.isEmpty()) {
                EmptyLoans()
            } else {
                loans.forEachIndexed { index, loan ->
                    val i = loan.inputs
                    val emi = runCatching {
                        Emi.calculate(i.principal.toDouble(), i.rate.toDouble(), i.unit.parseToMonths(i.tenure)!!).monthlyEmi
                    }.getOrNull()
                    ElevatedCard(
                        onClick = { principal = i.principal; rate = i.rate; months = i.tenure; unit = i.unit; fee = i.fee },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(loan.name, fontWeight = FontWeight.Bold)
                                Text(if (emi != null) stringResource(R.string.emi_value, HeroFormat.money(emi)) else stringResource(R.string.invalid_loan))
                                PayoffSection(loan, onMarkPaid = { milestone ->
                                    updateLoans(SavedLoans.markPaid(loans, index))
                                    if (milestone) (context as? android.app.Activity)?.let { reviews.maybeAsk(it, HappyMoment.PAYMENT_MILESTONE, hadError = false) }
                                })
                                ReminderSection(loan.reminder, onChange = { updateLoans(SavedLoans.setReminder(loans, index, it)) })
                            }
                            TextButton(onClick = { updateLoans(SavedLoans.removeAt(loans, index)) }) { Text(stringResource(R.string.delete)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (result != null) {
                if (effectiveRate != null) {
                    val feeText = stringResource(R.string.fee_amount_value, HeroFormat.money(feeAmount))
                    val costText = stringResource(R.string.total_cost_value, HeroFormat.money(ProcessingFee.totalCost(result.totalInterest, feeAmount)))
                    val rateText = stringResource(R.string.effective_rate_value, "%.2f".format(effectiveRate))
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics(mergeDescendants = true) {
                        contentDescription = "$feeText. $costText. $rateText"
                    }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(feeText); Text(costText); Text(rateText)
                        }
                    }
                }
                if (rateChange != null) {
                    val rateChangeText = stringResource(R.string.rate_change_result, HeroFormat.money(rateChange.newEmi), HeroFormat.money(rateChange.totalInterest))
                    Text(rateChangeText, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                } else if (changeRate.isNotBlank()) {
                    Text(stringResource(R.string.invalid_rate_change), color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                if (moratoriumResult != null) {
                    val moratoriumText = stringResource(
                        R.string.moratorium_result, HeroFormat.money(moratoriumResult.interestOnlyPayment),
                        HeroFormat.money(moratoriumResult.postEmi), HeroFormat.money(moratoriumResult.extraInterest),
                    )
                    Text(moratoriumText, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                } else if (moratorium.isNotBlank() && moratorium != "0") {
                    Text(stringResource(R.string.invalid_moratorium), color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                if (flatEquivalent != null) {
                    val flatText = stringResource(
                        R.string.flat_rate_result, HeroFormat.money(flatEquivalent.first),
                        "%.2f".format(flatEquivalent.second), "%.2f".format(flatEquivalent.third),
                    )
                    Text(flatText, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                } else if (flatRate.isNotBlank()) {
                    Text(stringResource(R.string.invalid_flat_rate), color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                if (impliedRate != null && impliedEmiValue != null) {
                    val impliedText = stringResource(
                        R.string.implied_rate_result, "%.2f".format(impliedRate),
                        HeroFormat.money(impliedEmiValue * n!! - p!!),
                    )
                    Text(impliedText, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                } else if (impliedEmi.isNotBlank()) {
                    Text(stringResource(R.string.invalid_implied_emi), color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                if (prepayment != null) {
                    SavingsCard(prepayment.interestSaved, prepayment.monthsSaved)
                } else if (prepay.isNotBlank()) {
                    Text(stringResource(R.string.invalid_prepayment), color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                Spacer(Modifier.height(12.dp))
                BalanceLineChart(ChartData.yearlyBalance(p!!, schedule))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.schedule), style = MaterialTheme.typography.titleMedium)
                val shareCsvChooser = stringResource(R.string.share_schedule_chooser)
                val shareCsvDescription = stringResource(R.string.share_schedule_description)
                TextButton(
                    onClick = {
                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, ScheduleCsv.build(schedule))
                        }
                        context.startActivity(android.content.Intent.createChooser(send, shareCsvChooser))
                    },
                    modifier = Modifier.semantics { contentDescription = shareCsvDescription },
                ) { Text(stringResource(R.string.share_schedule)) }
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
            Text(stringResource(R.string.monthly_emi), style = MaterialTheme.typography.labelLarge)
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
                    Text(stringResource(R.string.total_interest), style = MaterialTheme.typography.labelMedium)
                    Text(HeroFormat.money(result.totalInterest), fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(stringResource(R.string.total_payable), style = MaterialTheme.typography.labelMedium)
                    Text(HeroFormat.money(result.totalPayment), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyLoans() {
    val walletDescription = stringResource(R.string.empty_wallet)
    val color = MaterialTheme.colorScheme.outline
    Column(
        Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(72.dp).semantics { contentDescription = walletDescription }) {
            val w = 6.dp.toPx()
            drawRoundRect(color, Offset(w, size.height * 0.25f), Size(size.width - 2 * w, size.height * 0.6f),
                androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()), style = Stroke(w))
            drawLine(color, Offset(size.width * 0.6f, size.height * 0.55f), Offset(size.width * 0.8f, size.height * 0.55f),
                w, StrokeCap.Round)
        }
        Text(stringResource(R.string.no_saved_loans), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.no_saved_loans_hint), style = MaterialTheme.typography.bodySmall)
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
    val sliderDescription = stringResource(R.string.slider_description, label)
    Column(Modifier.padding(bottom = 8.dp)) {
        OutlinedTextField(
            value, { onChange(NumericInput.filter(it, decimals > 0)) }, label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = if (decimals > 0) KeyboardType.Decimal else KeyboardType.Number),
            singleLine = true,
            shape = FIELD_SHAPE,
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            value = range.position(value),
            onValueChange = { onChange(range.text(it, decimals)) },
            valueRange = range.min..range.max,
            modifier = Modifier.semantics { contentDescription = sliderDescription },
        )
    }
}

@Composable
private fun PayoffSection(loan: SavedLoan, onMarkPaid: (milestone: Boolean) -> Unit) {
    val progress = PayoffProgress.of(loan.inputs, loan.paidMonths) ?: return
    val percent = (progress.fractionPaid * 100).toInt()
    val done = progress.monthsLeft == 0
    val paidDescription = stringResource(R.string.percent_principal_paid, percent)
    var celebrate by remember { mutableStateOf<Int?>(null) }
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        val ringColor = MaterialTheme.colorScheme.primary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(Modifier.size(48.dp).padding(4.dp).semantics { contentDescription = paidDescription }) {
            val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            drawArc(trackColor, 0f, 360f, false, style = stroke)
            drawArc(ringColor, -90f, (progress.fractionPaid * 360).toFloat(), false, style = stroke)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(stringResource(R.string.paid_progress, percent, progress.paidMonths, progress.totalMonths))
            Text(
                if (done) stringResource(R.string.debt_free)
                else stringResource(R.string.months_left, progress.monthsLeft,
                    PayoffProgress.debtFreeDate(java.time.LocalDate.now(), progress.monthsLeft)
                        .format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", androidx.compose.ui.platform.LocalLocale.current.platformLocale))),
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
            if (m == 100) stringResource(R.string.loan_paid) else stringResource(R.string.milestone, m),
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    TextButton(
        enabled = !done,
        onClick = {
            val next = PayoffProgress.of(loan.inputs, PayoffProgress.markPaid(loan.inputs, loan.paidMonths))
            celebrate = next?.let { PayoffProgress.newMilestone(progress.fractionPaid, it.fractionPaid) }
            onMarkPaid(celebrate != null)
        },
    ) { Text(stringResource(R.string.mark_paid)) }
}

@Composable
private fun ReminderSection(reminder: Reminder?, onChange: (Reminder?) -> Unit) {
    val context = LocalContext.current
    var explain by remember { mutableStateOf(false) }
    val enable = { onChange(Reminder(java.time.LocalDate.now().dayOfMonth)) }
    val permission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { enable() } // Reminders are saved either way; the OS just hides them if notifications are denied.

    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(stringResource(R.string.remind_me), Modifier.weight(1f))
        Switch(checked = reminder != null, onCheckedChange = { on -> if (on) explain = true else onChange(null) })
    }
    if (reminder != null) {
        Stepper(stringResource(R.string.due_day), reminder.dueDay, Reminder.DUE_DAYS) { onChange(reminder.copy(dueDay = it)) }
        Stepper(stringResource(R.string.remind_days_before), reminder.daysBefore, Reminder.DAYS_BEFORE) { onChange(reminder.copy(daysBefore = it)) }
    }
    if (explain) {
        AlertDialog(
            onDismissRequest = { explain = false },
            title = { Text(stringResource(R.string.allow_reminders)) },
            text = { Text(stringResource(R.string.reminders_explain)) },
            confirmButton = {
                TextButton(onClick = {
                    explain = false
                    val needs = android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS,
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (needs) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS) else enable()
                }) { Text(stringResource(R.string.action_continue)) }
            },
            dismissButton = { TextButton(onClick = { explain = false }) { Text(stringResource(R.string.not_now)) } },
        )
    }
}

@Composable
private fun Stepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(stringResource(R.string.stepper_value, label, value), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        TextButton(enabled = value > range.first, onClick = { onChange(value - 1) }) { Text("−") }
        TextButton(enabled = value < range.last, onClick = { onChange(value + 1) }) { Text("+") }
    }
}

private val FIELD_SHAPE = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
