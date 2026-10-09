package com.sabin.emicalculator

import java.util.Locale
import kotlin.math.roundToInt

data class Shares(val principalPercent: Double, val interestPercent: Double)
data class BalancePoint(val year: Int, val balance: Double)

object ChartData {
    fun shares(principal: Double, interest: Double): Shares {
        val total = principal + interest
        if (total <= 0) return Shares(100.0, 0.0)
        val p = principal / total * 100
        return Shares(p, 100.0 - p)
    }

    /** Balance at the start (year 0) and at the end of each year; the last point is the final month. */
    fun yearlyBalance(principal: Double, schedule: List<ScheduleRow>): List<BalancePoint> {
        val points = mutableListOf(BalancePoint(0, principal))
        schedule.forEach { row ->
            val year = (row.month + 11) / 12
            if (row.month % 12 == 0 || row === schedule.last()) points.add(BalancePoint(year, row.balance))
        }
        return points
    }

    fun donutDescription(s: Shares): String =
        "Principal ${s.principalPercent.roundToInt()} percent, interest ${s.interestPercent.roundToInt()} percent of total payment"

    fun lineDescription(points: List<BalancePoint>): String =
        "Outstanding balance falls from ${money(points.first().balance)} to ${money(points.last().balance)} over ${points.last().year} years"

    private fun money(v: Double) = "%,.0f".format(Locale.US, v)
}
