package seng440.vaccinepassport

import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import seng440.vaccinepassport.passportreader.NFCListenerCallback
import seng440.vaccinepassport.ui.main.MainFragment
import seng440.vaccinepassport.ui.main.MainViewModel
import seng440.vaccinepassport.ui.main.ScannerFragment
import seng440.vaccinepassport.ui.main.SettingsFragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import seng440.vaccinepassport.reminders.ReminderUtils
import seng440.vaccinepassport.room.*
import seng440.vaccinepassport.ui.main.*
import java.util.*
import java.util.Collections.list

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()

    private val viewModel: VPassViewModel by viewModels() {
        VPassViewModelFactory((this.application as VPassLiveRoomApplication).repository)
    }
    private val reminderTimes : MutableList<Long> = ArrayList()
//    private val mNotificationTime = Calendar.getInstance().timeInMillis + 5000 //Set after 5 seconds from the current time.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (savedInstanceState == null) {
            var requirePin: Boolean = sharedPreferences.getBoolean("use_pin", false)

            if (requirePin) {
                Log.e("TAG", "requiring pin now")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LockScreenFragment.newInstance())
                        .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .addToBackStack("main")
                    .commit()
                if (sharedPreferences.getBoolean("border_mode", false)) {
                    findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scannow
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ScannerFragment.newInstance())
                        .addToBackStack("scanner")
                        .commit()
                }
            }
        }

        model.getActionBarTitle().observe(this, Observer<String>{ title ->
            supportActionBar?.title = title
        })
        model.getActionBarSubtitle().observe(this, Observer<String>{ subtitle ->
            supportActionBar?.subtitle = subtitle
        })
        model.gethideHeader().observe(this, Observer<Boolean>{ hide ->
            if (hide) { supportActionBar?.hide() } else { supportActionBar?.show() }
        })
        model.showBottomNavBar.observe(this, Observer<Boolean> { show ->
            findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility =
                if (show) View.VISIBLE else View.GONE
        })

        model.getActionBarTitle().value = getString(R.string.app_name)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menu_passports -> {
                    supportFragmentManager.popBackStack("main", 0)
                    true
                }
                R.id.menu_scannow -> {
                    if (supportFragmentManager.findFragmentByTag("scanner") != null) {
                        supportFragmentManager.popBackStack("scanner", 0)
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, ScannerFragment.newInstance())
                            .addToBackStack("scanner")
                            .commit()
                    }
                    true
                }
                R.id.menu_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, SettingsFragment())
                        .addToBackStack("settings")
                        .commit()
                    true
                }
                else -> false
            }
        }
        reminders()
    }

    fun reminders() {
        viewModel.Vpasses.observe(this) { newPasses ->
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val isBorderMode: Boolean = sharedPreferences.getBoolean("border_mode", false)
            if (!isBorderMode && newPasses.isNotEmpty()) {
                Log.d("REMINDERS", "New reminders size" + newPasses.size.toString())
                // Only interested in last pass in list, as can only upload one vaccine code at a time
                val pass = newPasses[newPasses.lastIndex]
                Log.d("REMINDERS", "Last scanned " + pass.toString())
                val passVaccineType = VaccineType.fromId(pass.vacId)
                if (passVaccineType != null) {
                    Log.d("REMINDERS", "Number of doses" + passVaccineType.numDoses.toString())
                    Log.d("REMINDERS", "Dose number" + pass.dosageNum)
                    if (pass.dosageNum < passVaccineType.numDoses) {
                        Log.d("REMINDERS", "Add a new reminder")
                        ReminderUtils().setReminder(Calendar.getInstance().timeInMillis, this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            var tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                val fragment = supportFragmentManager.findFragmentByTag("show_scan_result")
                if (fragment != null && fragment is NFCListenerCallback) {
                    fragment.onAvailableNFC(tag)
                }
            }
        }
    }

    override fun onBackPressed() {
        val currentFrag = supportFragmentManager.findFragmentById(R.id.container)
        if (currentFrag is MainFragment || currentFrag is LockScreenFragment) {
            finish()
        } else if (currentFrag is ScannedBarcodeFragment){
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            if (model.getShowingBarcodeInScannedBarcodeFragment().value == true || !sharedPreferences.getBoolean("border_mode", false)) {
                findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_passports
            } else {
                findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scannow
            }
        } else {
            findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_passports
        }
    }

    companion object {
        fun timestampToDate(timestamp: Int): Date {
            Log.d("DATE", timestamp.toString())
            return Date(timestamp.toLong() * 86400L * 1000L)
        }
    }
}