package com.sabin.emicalculator

import kotlin.math.ceil

/** Net saving is interest saved minus [fee]; [breakEvenMonth] is null when switching never pays back. */
data class LoanTransferResult(
    val currentEmi: Double,
    val newEmi: Double,
    val currentInterest: Double,
    val newInterest: Double,
    val fee: Double,
    val netSaving: Double,
    val breakEvenMonth: Int?,
)

object LoanTransfer {
    private const val EPS = 0.005

    fun feeFromPercent(balance: Double, percent: Double): Double {
        require(balance > 0) { "Balance must be positive" }
        require(percent >= 0) { "Fee cannot be negative" }
        return balance * percent / 100
    }

    fun calculate(
        balance: Double, currentRate: Double, remainingMonths: Int,
        newRate: Double, newMonths: Int, fee: Double,
    ): LoanTransferResult {
        require(fee >= 0) { "Fee cannot be negative" }
        val current = Emi.calculate(balance, currentRate, remainingMonths)
        val new = Emi.calculate(balance, newRate, newMonths)
        val net = current.totalInterest - new.totalInterest - fee
        var breakEven: Int? = null
        if (net > EPS) {
            // Cumulative cash out of staying vs switching (fee paid up front); last month still behind, plus one.
            var behind = 0
            for (m in 1..maxOf(remainingMonths, newMonths)) {
                val stay = current.monthlyEmi * minOf(m, remainingMonths)
                val switch = fee + new.monthlyEmi * minOf(m, newMonths)
                if (stay - switch < -EPS) behind = m
            }
            breakEven = behind + 1
        }
        return LoanTransferResult(current.monthlyEmi, new.monthlyEmi, current.totalInterest, new.totalInterest, fee, net, breakEven)
    }

    /** Plain verdict; [progress] (0..1) scales the figure for the count-up animation. */
    fun verdict(r: LoanTransferResult, progress: Float = 1f): String {
        val p = progress.coerceIn(0f, 1f).toDouble()
        return when {
            r.netSaving > EPS -> "Saves NPR %,.2f after fees, paid back in month %d".format(r.netSaving * p, r.breakEvenMonth)
            r.netSaving < -EPS && r.currentInterest - r.newInterest > EPS ->
                "Costs you more: NPR %,.2f extra. The fee does not pay back within the tenure".format(-r.netSaving * p)
            r.netSaving < -EPS -> "Costs you more: NPR %,.2f extra".format(-r.netSaving * p)
            else -> "No difference: switching saves nothing"
        }
    }
}
