package com.sabin.emicalculator

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.time.LocalDate

/** Home-screen widget: next EMI and debt-free countdown for the main saved loan. Tapping opens the app. */
class EmiWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetState.of(
            SavedLoans.deserialize(context.getSharedPreferences(LOANS_PREFS, Context.MODE_PRIVATE).getString(LOANS_KEY, null)),
            LocalDate.now(),
        )
        provideContent {
            GlanceTheme(colors = if (android.os.Build.VERSION.SDK_INT >= 31) GlanceTheme.colors else brandColors) {
                WidgetContent(state)
            }
        }
    }

    companion object {
        const val LOANS_PREFS = "emi_loans"
        const val LOANS_KEY = "loans"

        private val brandColors = ColorProviders(light = brandLightColorScheme(), dark = brandDarkColorScheme())

        suspend fun refresh(context: Context) = EmiWidget().updateAll(context)
    }
}

@Composable
private fun WidgetContent(state: WidgetState) {
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(12.dp)
            .clickable(actionStartActivity(android.content.Intent(LocalContext.current, MainActivity::class.java))),
    ) {
        val primary = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        val secondary = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp)
        when (state) {
            WidgetState.Empty -> {
                Text("EMI Calculator", style = primary)
                Text("Save a loan to track your next EMI", style = secondary)
            }
            is WidgetState.Active -> {
                Text(state.loanName, style = secondary)
                Text(state.emi, style = primary)
                if (state.done) {
                    Text("Debt free! 🎉", style = secondary)
                } else {
                    Text("Next EMI due ${state.nextDue}", style = secondary)
                    Text("${state.monthsLeft} months left · debt-free ${state.debtFree}", style = secondary)
                }
            }
        }
    }
}

class EmiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EmiWidget()
}
