package com.sabin.emicalculator

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Schedules one inexact alarm (no exact-alarm permission) for the next reminder day, at 9am local time. */
object ReminderAlarm {
    private const val CHANNEL = "emi_reminders"

    private fun loans(context: Context) = SavedLoans.deserialize(
        context.getSharedPreferences(EmiWidget.LOANS_PREFS, Context.MODE_PRIVATE).getString(EmiWidget.LOANS_KEY, null),
    )

    private fun intent(context: Context) = PendingIntent.getBroadcast(
        context, 0, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** Replaces any pending alarm with one for the earliest reminder, or cancels it when none are enabled. */
    fun reschedule(context: Context, today: LocalDate = LocalDate.now()) {
        val alarms = context.getSystemService(AlarmManager::class.java)
        val pending = intent(context)
        val plan = ReminderSchedule.plan(loans(context), today)
        if (plan == null) { alarms.cancel(pending); return }
        val at = plan.date.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarms.set(AlarmManager.RTC, at, pending)
    }

    fun notifyDue(context: Context, today: LocalDate = LocalDate.now()) {
        val plan = ReminderSchedule.plan(loans(context), today)?.takeIf { it.date == today } ?: return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "EMI reminders", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = android.app.Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("EMI due soon")
            .setContentText(plan.loanNames.joinToString(", "))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        // Silently skipped by the OS if POST_NOTIFICATIONS was denied or revoked.
        manager.notify(1, notification)
    }
}

/** Fires the daily reminder, then arms the next one. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderAlarm.notifyDue(context)
        // Move past today so the same reminder is not rescheduled for today again.
        ReminderAlarm.reschedule(context, LocalDate.now().plusDays(1))
    }
}

/** Alarms are cleared on reboot; re-arm them. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) ReminderAlarm.reschedule(context)
    }
}
