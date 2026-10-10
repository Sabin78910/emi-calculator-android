package com.sabin.emicalculator

import androidx.compose.ui.res.stringResource
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val ART = listOf(R.drawable.onboarding_emi, R.drawable.onboarding_plan, R.drawable.onboarding_try)

/** Three-page first-run intro; the last page lets the user calculate a first EMI. Calls [onFinish] once done or skipped. */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val pages = Onboarding.PAGES
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    var principal by remember { mutableStateOf(SavedInputs.DEFAULT.principal) }

    fun finish() {
        context.getSharedPreferences(Onboarding.PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(Onboarding.KEY_DONE, true).apply()
        context.getSharedPreferences("emi_inputs", Context.MODE_PRIVATE)
            .edit().putString("principal", principal).apply()
        onFinish()
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.systemBarsPadding().padding(16.dp)) {
            Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End) {
                if (Onboarding.showSkip(pager.currentPage)) TextButton(onClick = ::finish) { Text(stringResource(R.string.skip)) }
            }
            HorizontalPager(pager, Modifier.weight(1f)) { i ->
                val page = pages[i]
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(painterResource(ART[i]), contentDescription = null, Modifier.size(220.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(page.title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(page.benefit), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    if (page.isAction) {
                        Spacer(Modifier.height(16.dp))
                        SliderField(stringResource(R.string.loan_amount), principal, SliderRange.AMOUNT, 0) { principal = it }
                        val d = SavedInputs.DEFAULT
                        val emi = runCatching {
                            Emi.calculate(principal.toDouble(), d.rate.toDouble(), d.tenure.toDouble().toInt())
                        }.getOrNull()
                        if (emi != null) {
                            Text(
                                stringResource(R.string.onboarding_emi, HeroFormat.money(emi.monthlyEmi)),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { if (pager.currentPage == pages.lastIndex) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(if (pager.currentPage == pages.lastIndex) R.string.continue_to_calculator else R.string.next)) }
        }
    }
}
