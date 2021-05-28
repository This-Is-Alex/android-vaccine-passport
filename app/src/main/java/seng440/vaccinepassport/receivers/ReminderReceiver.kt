package seng440.vaccinepassport.receivers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R

fun Bundle.toParamsString() = keySet().map { "$it -> ${get(it)}" }.joinToString("\n")

//
// Step 1
//
class ReminderReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ServiceCast")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("FOO", "Received message ${intent.action} with\n${intent.extras?.toParamsString()}")

        val intent: PendingIntent = Intent(context, MainActivity::class.java).run {
            PendingIntent.getActivity(context, 0, this, 0)
        }

        val notification = Notification.Builder(context, Notification.CATEGORY_REMINDER).run {
            setSmallIcon(R.drawable.icon_syringe)
            setContentTitle("A new day, a new memory")
            setContentText("Just a friendly reminder to take today's picture.")
            setContentIntent(intent)
            setAutoCancel(true)
            build()
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, notification)
    }
}