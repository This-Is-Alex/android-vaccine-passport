package seng440.vaccinepassport.ui.main

import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import seng440.vaccinepassport.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val hasPin: Boolean = sharedPreferences.getString("pin", "none") != "none"

        preferenceScreen.findPreference<SwitchPreference>("use_pin")?.isEnabled = hasPin

        preferenceScreen.findPreference<Preference>("set_pin")?.setOnPreferenceClickListener {

            val builder = android.app.AlertDialog.Builder(context)

            val pin_entry = layoutInflater.inflate(R.layout.lock_screen_fragment, null, false)
            builder.setView(pin_entry)

            var pinDisplay: TextView = pin_entry.findViewById(R.id.pinDisplay)
            pinDisplay.text = sharedPreferences.getString("pin", "")

            var confirmButton: Button = pin_entry.findViewById(R.id.lockButtonConfirm)
            confirmButton.visibility = View.GONE

            pin_entry.findViewById<Button>(R.id.lockButton1)?.setOnClickListener {
                numPress("1", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton2)?.setOnClickListener {
                numPress("2", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton3)?.setOnClickListener {
                numPress("3", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton4)?.setOnClickListener {
                numPress("4", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton5)?.setOnClickListener {
                numPress("5", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton6)?.setOnClickListener {
                numPress("6", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton7)?.setOnClickListener {
                numPress("7", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton8)?.setOnClickListener {
                numPress("8", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton9)?.setOnClickListener {
                numPress("9", pinDisplay)
            }
            pin_entry.findViewById<Button>(R.id.lockButton0)?.setOnClickListener {
                numPress("0", pinDisplay)
            }

            pin_entry.findViewById<Button>(R.id.lockButtonDelete)?.setOnClickListener {
                numPress("delete", pinDisplay)
            }

            builder.setPositiveButton("Confirm Pin") { _, _ ->
                val editor: Editor = sharedPreferences.edit()
                editor.putString("pin", pinDisplay.text.toString())
                editor.commit()
            }
            builder.show()

            true
        }


    }
    fun numPress(num: String, pinDisplay: TextView) {
        Log.e("TAG", "Require pin:${num}")
        val display = pinDisplay.text.toString()
        if (num == "delete") {
            if (display != null && display.length > 0) {
                pinDisplay.text = display.substring(0, display.length - 1);
            }
        } else {
            pinDisplay.text =  display + num;
        }
    }
}