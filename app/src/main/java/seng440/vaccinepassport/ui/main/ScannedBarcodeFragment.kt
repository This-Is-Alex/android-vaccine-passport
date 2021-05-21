package seng440.vaccinepassport.ui.main

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass
import seng440.vaccinepassport.VaccineType
import seng440.vaccinepassport.passportreader.*
import seng440.vaccinepassport.room.VPassData
import seng440.vaccinepassport.room.VPassLiveRoomApplication
import seng440.vaccinepassport.room.VPassViewModel
import seng440.vaccinepassport.room.VPassViewModelFactory
import java.security.Security
import java.text.SimpleDateFormat


class ScannedBarcodeFragment : Fragment(), NFCListenerCallback, PassportReaderCallback {
    private val model: MainViewModel by activityViewModels()
    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy")
    private lateinit var passport: SerializableVPass
    private var readingPassport: Boolean = false

    private val viewModel: VPassViewModel by activityViewModels() {
        VPassViewModelFactory((activity?.application as VPassLiveRoomApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())

        if (requireActivity().intent.hasExtra("just_scanned")) {
            passport = requireActivity().intent.getSerializableExtra("just_scanned") as SerializableVPass
        }
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.showing_scan_result_title)
        model.getActionBarSubtitle().value = ""
        model.gethideHeader().value = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scanned_barcode, container, false)

        view.findViewById<CardView>(R.id.barcode_save_options).addView(setupSaveCancelView(inflater))

        return view
    }

    private fun setupSaveCancelView(inflater: LayoutInflater): View {
        val buttonMenuView = inflater.inflate(R.layout.layout_barcode_save_cancel, null, false)

        buttonMenuView.findViewById<Button>(R.id.save_barcode_btn).setOnClickListener(View.OnClickListener {
            val dataObject = VPassData(
                passport.date,
                passport.vacId,
                passport.drAdministered,
                passport.dosageNum,
                passport.name,
                passport.passportNum,
                passport.passportExpDate,
                passport.dob,
                passport.country)
            viewModel.deleteVPass(dataObject)
            viewModel.addVPass(dataObject)

            Toast.makeText(context, "DEBUG: Saved successfully (please remove this one day)", Toast.LENGTH_SHORT).show()
            goBack()
        })

        buttonMenuView.findViewById<Button>(R.id.cancel_barcode_btn).setOnClickListener(View.OnClickListener {
            goBack()
        })

        return buttonMenuView
    }

    private fun goBack() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        val borderMode: Boolean = sharedPreferences.getBoolean("border_mode", false)
        val destination = if (borderMode) "scanner" else "main"

        requireActivity().supportFragmentManager.popBackStack(destination, 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = MainActivity.timestampToDate(passport.date)
        val dob = MainActivity.timestampToDate(passport.dob)
        val passportExpiry = MainActivity.timestampToDate(passport.passportExpDate)


        view.findViewById<TextView>(R.id.vpass_vaccine_date).text = dateFormatter.format(date)
        view.findViewById<TextView>(R.id.vpass_vaccine_type).text = VaccineType.fromId(passport.vacId)?.fullName
        view.findViewById<TextView>(R.id.vpass_vaccine_giver).text = passport.drAdministered
        view.findViewById<TextView>(R.id.vpass_vaccine_country).text = passport.country
        view.findViewById<TextView>(R.id.vpass_vaccine_dose).text = passport.dosageNum.toString()

        view.findViewById<TextView>(R.id.vpass_passport_passportno).text = passport.passportNum
        view.findViewById<TextView>(R.id.vpass_passport_passportexpiry).text = dateFormatter.format(passportExpiry)

        view.findViewById<TextView>(R.id.vpass_person_name).text = passport.name
        view.findViewById<TextView>(R.id.vpass_person_dob).text = dateFormatter.format(dob)
    }

    override fun onResume() {
        super.onResume()
        val adapter = NfcAdapter.getDefaultAdapter(requireContext())
        Log.d("Passport", "Firing up NFC")
        if (adapter != null) {
            if (!adapter.isEnabled) {
                requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_disabled)
                requireView().findViewById<CardView>(R.id.vpass_nfc_card).setBackgroundResource(R.color.nfc_none)
            } else {
                if (!readingPassport) {
                    requireView().findViewById<TextView>(R.id.vpass_nfc_status).text =
                        getString(R.string.nfc_ready)
                    requireView().findViewById<CardView>(R.id.vpass_nfc_card)
                        .setBackgroundResource(R.color.nfc_ready)
                }
                val intent = Intent(requireActivity(), requireActivity().javaClass)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                val pendingIntent =
                    PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val filter = arrayOf(arrayOf("android.nfc.tech.IsoDep"))
                adapter.enableForegroundDispatch(requireActivity(), pendingIntent, null, filter)
            }
        } else {
            requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_nonexistent)
            requireView().findViewById<TextView>(R.id.vpass_nfc_card).setBackgroundResource(R.color.nfc_none)
        }
    }

    override fun onPause() {
        super.onPause()
        val adapter = NfcAdapter.getDefaultAdapter(requireContext())
        adapter?.disableForegroundDispatch(requireActivity())
    }

    override fun onAvailableNFC(tag: Tag) {
        readingPassport = true
        Log.d("Passport", "Got tag")
        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_reading)
        requireView().findViewById<CardView>(R.id.vpass_nfc_card).setBackgroundResource(R.color.nfc_ready)
        lifecycleScope.launch {
            val task = PassportTask().readTag(tag, passport, requireContext(), getBarcodeCallback())
        }
    }

    private fun getBarcodeCallback(): PassportReaderCallback {
        return this
    }

    override fun onReadSuccess(passport: IDPassport) {
        readingPassport = false
        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_success)
            .replace("%NAME%", passport.fullName!!)
        requireView().findViewById<CardView>(R.id.vpass_nfc_card).setBackgroundResource(R.color.nfc_success)
    }

    override fun onReadFailure(msg: String) {
        readingPassport = false
        var message = msg
        if (message == "Tag was lost.") {
            message = getString(R.string.nfc_taglost_error)
        }
        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_error)
            .replace("%REASON%", message)
        requireView().findViewById<CardView>(R.id.vpass_nfc_card).setBackgroundResource(R.color.nfc_fail)
    }
}