package seng440.vaccinepassport.reminders

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import java.util.*

class ReminderUtils {

    fun setReminder(timeInMilliSeconds: Long, activity: Activity) {
        Log.d("REMINDERS", "Time in MilliSeconds" + timeInMilliSeconds.toString())
        if (timeInMilliSeconds > 0) {
            val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(activity.applicationContext, ReminderReceiver::class.java)
            alarmIntent.putExtra("reason", "notification")
            alarmIntent.putExtra("timestamp", timeInMilliSeconds)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMilliSeconds
            val pendingIntent = PendingIntent.getBroadcast(activity, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}