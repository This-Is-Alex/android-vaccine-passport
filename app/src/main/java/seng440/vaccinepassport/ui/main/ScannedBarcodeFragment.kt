package seng440.vaccinepassport.ui.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass
import seng440.vaccinepassport.VaccineType
import seng440.vaccinepassport.room.VPassData
import seng440.vaccinepassport.room.VPassLiveRoomApplication
import seng440.vaccinepassport.room.VPassViewModel
import seng440.vaccinepassport.room.VPassViewModelFactory
import java.text.SimpleDateFormat

class ScannedBarcodeFragment : Fragment() {
    private val model: MainViewModel by activityViewModels()
    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy")
    private lateinit var passport: SerializableVPass

    private val viewModel: VPassViewModel by activityViewModels() {
        VPassViewModelFactory((activity?.application as VPassLiveRoomApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    fun setupSaveCancelView(inflater: LayoutInflater): View {
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
}