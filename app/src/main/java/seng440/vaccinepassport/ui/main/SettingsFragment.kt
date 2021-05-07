package seng440.vaccinepassport.ui.main

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import seng440.vaccinepassport.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}