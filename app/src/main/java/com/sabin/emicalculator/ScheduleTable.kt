package com.sabin.emicalculator

import java.util.Locale

object ScheduleTable {
    val HEADER = listOf("Month", "Principal", "Interest", "Balance")

    fun cells(row: ScheduleRow): List<String> = listOf(
        row.month.toString(),
        "%,.0f".format(Locale.US, row.principal),
        "%,.0f".format(Locale.US, row.interest),
        "%,.0f".format(Locale.US, row.balance),
    )
}
