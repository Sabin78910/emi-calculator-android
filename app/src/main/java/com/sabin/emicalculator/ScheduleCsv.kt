package com.sabin.emicalculator

import java.util.Locale

object ScheduleCsv {
    const val HEADER = "Month,EMI,Principal,Interest,Balance"

    private fun money(v: Double) = "%.2f".format(Locale.US, v)

    fun build(schedule: List<ScheduleRow>): String = buildString {
        append(HEADER).append('\n')
        schedule.forEach { r ->
            append(r.month).append(',')
                .append(money(r.principal + r.interest)).append(',')
                .append(money(r.principal)).append(',')
                .append(money(r.interest)).append(',')
                .append(money(r.balance)).append('\n')
        }
    }
}
