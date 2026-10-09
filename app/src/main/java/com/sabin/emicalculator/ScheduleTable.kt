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

    fun description(row: ScheduleRow): String {
        val c = cells(row)
        return "Month ${c[0]}, principal ${c[1]}, interest ${c[2]}, balance ${c[3]}"
    }
}
