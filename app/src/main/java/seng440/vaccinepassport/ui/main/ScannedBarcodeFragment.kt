package seng440.vaccinepassport.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
import seng440.vaccinepassport.VaccineType
import seng440.vaccinepassport.room.VPassData
import java.text.SimpleDateFormat

class ScannedBarcodeFragment(val passport: VPassData) : Fragment() {
    private val model: MainViewModel by activityViewModels()
    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy")

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.showing_scan_result_title)
        model.getActionBarSubtitle().value = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanned_barcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = MainActivity.timestampToDate(passport.date)
        val dob = MainActivity.timestampToDate(passport.dob)
        val passportExpiry = MainActivity.timestampToDate(passport.passportExpDate)


        view.findViewById<TextView>(R.id.vpass_vaccine_date).text = dateFormatter.format(date)
        view.findViewById<TextView>(R.id.vpass_vaccine_type).text = VaccineType.fromId(passport.vacId)?.fullName
        view.findViewById<TextView>(R.id.vpass_vaccine_giver).text = passport.drAdministered
        view.findViewById<TextView>(R.id.vpass_vaccine_dose).text = passport.dosageNum.toString()

        view.findViewById<TextView>(R.id.vpass_passport_passportno).text = passport.passportNum
        view.findViewById<TextView>(R.id.vpass_passport_passportexpiry).text = dateFormatter.format(passportExpiry)

        view.findViewById<TextView>(R.id.vpass_person_name).text = passport.name
        view.findViewById<TextView>(R.id.vpass_person_dob).text = dateFormatter.format(dob)
    }
}