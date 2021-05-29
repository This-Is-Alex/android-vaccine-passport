package seng440.vaccinepassport.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, ReminderService::class.java)
        service.putExtra("reason", intent.getStringExtra("reason"))
        service.putExtra("timestamp", intent.getLongExtra("timestamp", 0))
        context.startService(service)
    }

}