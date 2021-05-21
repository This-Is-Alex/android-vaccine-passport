package seng440.vaccinepassport

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import seng440.vaccinepassport.passportreader.NFCListenerCallback
import seng440.vaccinepassport.ui.main.MainFragment
import seng440.vaccinepassport.ui.main.MainViewModel
import seng440.vaccinepassport.ui.main.ScannerFragment
import seng440.vaccinepassport.ui.main.SettingsFragment
import androidx.preference.PreferenceManager
import seng440.vaccinepassport.ui.main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            var requirePin: Boolean = sharedPreferences.getBoolean("use_pin", false)

            Log.e("TAG", "Require pin:${requirePin}")

            if (requirePin) {
                Log.e("TAG", "requiring pin now")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LockScreenFragment.newInstance())
                        .addToBackStack("lockScreen")
                        .commit()
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, MainFragment.newInstance())
                        .addToBackStack("main")
                        .commit()
            }
            //TODO show PIN/fingerprint unlock when set
        }
        val model: MainViewModel by viewModels()
        model.getActionBarTitle().observe(this, Observer<String>{ title ->
            supportActionBar?.title = title
        })
        model.getActionBarSubtitle().observe(this, Observer<String>{ subtitle ->
            supportActionBar?.subtitle = subtitle
        })
        model.gethideHeader().observe(this, Observer<Boolean>{ hide ->
            if (hide) { supportActionBar?.hide() } else { supportActionBar?.show() }
        })

        model.getActionBarTitle().value = getString(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container,
                        SettingsFragment()
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("scanner")
                    .commit()
                true
            }
            R.id.menu_scan_temp -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ScannerFragment.newInstance())
                    .addToBackStack("scanner")
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
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


    companion object {
        fun timestampToDate(timestamp: Int): Date {
            return Date(timestamp.toLong() * 86400L * 1000L)
        }
    }
}