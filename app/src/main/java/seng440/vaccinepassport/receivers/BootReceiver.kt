package seng440.vaccinepassport.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import seng440.vaccinepassport.Utilities

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Utilities.scheduleReminder(context, ?, ?)

        //
        // Step 12
        //
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getInt("hour", -1) >= 0) {
            Utilities.scheduleReminder(context, prefs.getInt("hour", 6), prefs.getInt("minute", 0))
        }
    }
}