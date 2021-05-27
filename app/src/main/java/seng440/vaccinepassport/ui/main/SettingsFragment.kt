package seng440.vaccinepassport.ui.main

import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import seng440.vaccinepassport.R


class SettingsFragment : PreferenceFragmentCompat() {

    private val model: MainViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val hasPin: Boolean = sharedPreferences.getString("pin", "none") != "none"
        var requirePin: Boolean = sharedPreferences.getBoolean("use_pin", false)
        val isBorderMode: Boolean = sharedPreferences.getBoolean("border_mode", false)

        preferenceScreen.findPreference<SwitchPreference>("logging_mode")?.isEnabled = isBorderMode
        preferenceScreen.findPreference<SwitchPreference>("use_pin")?.isEnabled = hasPin

        preferenceScreen.findPreference<Preference>("use_pin")?.setOnPreferenceClickListener {
            preferenceScreen.findPreference<SwitchPreference>("use_fingerprint")?.isEnabled = sharedPreferences.getBoolean("use_pin", false)
            true
        }

        preferenceScreen.findPreference<SwitchPreference>("use_fingerprint")?.isEnabled = hasPin && requirePin

        preferenceScreen.findPreference<Preference>("set_pin")?.setOnPreferenceClickListener {

            val builder = android.app.AlertDialog.Builder(context)

            val pin_entry = layoutInflater.inflate(R.layout.lock_screen_fragment, null, false)
            builder.setView(pin_entry)

            var pinDisplay: TextView = pin_entry.findViewById(R.id.pinDisplay)
            pinDisplay.text = sharedPreferences.getString("pin", "")

            var confirmButton: Button = pin_entry.findViewById(R.id.lockButtonConfirm)
            confirmButton.visibility = View.GONE
            var bioButton: Button = pin_entry.findViewById(R.id.lockButtonFinger)
            bioButton.visibility = View.GONE

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
                preferenceScreen.findPreference<SwitchPreference>("use_pin")?.isEnabled = true
                editor.commit()
            }
            builder.show()

            true
        }

        preferenceScreen.findPreference<Preference>("border_mode")?.setOnPreferenceClickListener {
            preferenceScreen.findPreference<SwitchPreference>("logging_mode")?.isEnabled = sharedPreferences.getBoolean("border_mode", false)
            true
        }

    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.menu_settings)
        model.getActionBarSubtitle().value = ""
        model.gethideHeader().value = false
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