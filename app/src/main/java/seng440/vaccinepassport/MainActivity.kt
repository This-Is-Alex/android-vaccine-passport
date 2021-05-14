package seng440.vaccinepassport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import seng440.vaccinepassport.listeners.BarcodeScannedListener
import seng440.vaccinepassport.room.VPassData
import seng440.vaccinepassport.ui.main.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*

class MainActivity : AppCompatActivity(), BarcodeScannedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
            //TODO show PIN/fingerprint unlock when set
        }
        val model: MainViewModel by viewModels()
        model.getActionBarTitle().observe(this, Observer<String>{ title ->
            supportActionBar?.title = title
        })
        model.getActionBarSubtitle().observe(this, Observer<String>{ subtitle ->
            supportActionBar?.subtitle = subtitle
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

    override fun onScanned(rawData: ByteArray) {
        val inputStream: DataInputStream = DataInputStream(ByteArrayInputStream(rawData))

        val dateAdministered = inputStream.readInt()
        val vaccineType = VaccineType.fromId(inputStream.readByte())
        val dosageNumber = inputStream.readByte().toInt()

        val passportNumberRaw = ByteArray(9)
        inputStream.read(passportNumberRaw, 0, 9)

        val passportNumber = String(passportNumberRaw)
            .trimEnd(Integer.valueOf(0).toChar()) //trim 0s off the end
        val passportExpiry = inputStream.readInt()
        val dateOfBirth = inputStream.readInt()

        val countryRaw = ByteArray(3)
        inputStream.read(countryRaw, 0, 3)

        val country = String(countryRaw)

        Log.d("Barcode", "About to read names... $dateAdministered ${vaccineType?.fullName} $dosageNumber $passportNumber")

        val rawName = ByteArray(inputStream.readByte().toInt())
        inputStream.read(rawName, 0, rawName.size)
        val rawDoctor = ByteArray(inputStream.readByte().toInt())
        inputStream.read(rawDoctor, 0, rawDoctor.size)

        val name = String(rawName)
        val doctorName = String(rawDoctor)

        Log.d("Barcode", "Names are $name, $doctorName")

        if (inputStream.read() != -1) return //there is still more data, must be the wrong format
        if (vaccineType == null || !isLettersOrDigits(passportNumber) || !isLettersOrDigits(country)) return

        Log.d("Barcode", "Read successfully")

        val dataObject = SerializableVPass(dateAdministered, vaccineType.id, doctorName, dosageNumber.toShort(), name, passportNumber, passportExpiry, dateOfBirth, country)
        intent.putExtra("just_scanned", dataObject)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container,
                ScannedBarcodeFragment()
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("show_scan_result")
            .commit()
    }

    private fun isLettersOrDigits(chars: String): Boolean {
        for (c in chars) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }

    companion object {
        fun timestampToDate(timestamp: Int): Date {
            return Date(timestamp.toLong() * 86400L * 1000L)
        }
    }
}