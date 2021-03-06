package seng440.vaccinepassport

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import seng440.vaccinepassport.passportreader.NFCListenerCallback
import seng440.vaccinepassport.ui.main.MainFragment
import seng440.vaccinepassport.ui.main.MainViewModel
import seng440.vaccinepassport.ui.main.ScannerFragment
import seng440.vaccinepassport.ui.main.SettingsFragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import seng440.vaccinepassport.reminders.ReminderUtils
import seng440.vaccinepassport.room.*
import seng440.vaccinepassport.ui.main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()

    private val viewModel: VPassViewModel by viewModels() {
        VPassViewModelFactory((this.application as VPassLiveRoomApplication).repository)
    }
    private val reminderTimes : MutableList<Long> = ArrayList()
//    private val mNotificationTime = Calendar.getInstance().timeInMillis + 5000 //Set after 5 seconds from the current time.

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("TAG", "Launching with: ${intent.action}")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (savedInstanceState == null) {
            val requirePin: Boolean = sharedPreferences.getBoolean("use_pin", false)

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
                if ("android.intent.custom.scan" == intent.action) {
                    findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scan_now
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.container, ScannerFragment.newInstance())
                            .addToBackStack("scanner")
                            .commit()
                } else if ("android.intent.custom.show_latest" == intent.action) {
                    showLatest()
                } else if (sharedPreferences.getBoolean("border_mode", false)) {
                        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scan_now
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
            if (show) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER)
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
        })

        model.getActionBarTitle().value = getString(R.string.app_name)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menu_passports -> {
                    supportFragmentManager.popBackStack("main", 0)
                    true
                }
                R.id.menu_scan_now -> {
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
            val notifications: Boolean = sharedPreferences.getBoolean("notifications", true)
            Log.d("NOTIFICATIONS", "Allow notifications set to " + notifications)
            if (!isBorderMode && newPasses.isNotEmpty() && notifications) {
                Log.d("REMINDERS", "New reminders size" + newPasses.size.toString())
                // Only interested in last pass in list, as can only upload one vaccine code at a time
                val pass = newPasses[newPasses.lastIndex]
                Log.d("REMINDERS", "Last scanned " + pass.toString())
                val passVaccineType = VaccineType.fromId(pass.vacId)
                if (passVaccineType != null) {
                    Log.d("REMINDERS", "Number of doses" + passVaccineType.numDoses.toString())
                    Log.d("REMINDERS", "Dose number" + pass.dosageNum)
                    if (pass.dosageNum < passVaccineType.numDoses) {
                        // Not sure if these date calculations are correct
                        Log.d("REMINDERS", "Add a new reminder")
                        val vaccineDate = timestampToDate(pass.date + passVaccineType.daysBetweenDoses!!)
                        Log.d("REMINDERS", "Vaccine Date " + vaccineDate)
                        val calendar = Calendar.getInstance()
                        calendar.set((vaccineDate.year + 1900), vaccineDate.month, vaccineDate.day,
                                15, 57, 0)
                        Log.d("REMINDERS", "Calendar date" + calendar.time)
                        ReminderUtils().setReminder(calendar.timeInMillis, this@MainActivity)
                        Log.d("REMINDERS", "Reminder set")
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
                findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scan_now
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

    private fun showLatest() {
        Log.i("CLICK", "Displaying Data for latest")
        Log.i("CLICK", "Displaying Data for " + viewModel.Vpasses)

        lifecycleScope.launch {
            var dataObject: VPassData? = null
            withContext(Dispatchers.IO) {
                dataObject = try {
                    viewModel.getLatest()
                } catch (exception: Exception) {
                    null
                }
            }
            if (dataObject != null) {
                val cerealObject = getSerialisedVPass(dataObject!!)
                model.barcodeToDisplay.value = cerealObject
                model.getShowingBarcodeInScannedBarcodeFragment().value = true
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container,
                                ScannedBarcodeFragment(),
                                "show_scan_result"
                        )
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("show_scan_result")
                        .commit()
            } else {
                makeToast(getString(R.string.no_pass_shortcut))
            }
        }
    }

    fun makeToast(string: String){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    private fun getSerialisedVPass(vpass: VPassData) : SerializableVPass {
        Log.i("VPASS Received", vpass.name)
        val dateAdministered = vpass.date
        val vaccineType = vpass.vacId
        val dosageNumber = vpass.dosageNum
        val passportNumber = vpass.passportNum
        val passportExpiry = vpass.passportExpDate
        val dateOfBirth = vpass.dob
        val country = vpass.country
        val name = vpass.name
        val doctorName = vpass.drAdministered
        return SerializableVPass(dateAdministered, vaccineType, doctorName, dosageNumber, name, passportNumber, passportExpiry, dateOfBirth, country)
    }
}