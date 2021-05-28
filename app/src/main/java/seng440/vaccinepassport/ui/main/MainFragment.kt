package seng440.vaccinepassport.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass
import seng440.vaccinepassport.room.*

class MainFragment : Fragment(), VPassAdapter.OnVPassListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: VPassViewModel by activityViewModels() {
        VPassViewModelFactory((activity?.application as VPassLiveRoomApplication).repository)
    }

    private val model: MainViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view  = inflater.inflate(R.layout.main_fragment, container, false)
        val adapter = VPassAdapter(listOf(), this, requireContext())
        viewModel.Vpasses.observe(viewLifecycleOwner) { newPasses ->
            adapter.setData(newPasses)
        }
        val recycler : RecyclerView = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter
        return view
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.showing_vaccine_passports)
        model.getActionBarSubtitle().value = ""
        model.gethideHeader().value = false
    }

    override fun onVPassClick(position: Int) {
        Log.i("CLICK", "Displaying Data for " + viewModel.Vpasses.value!![position].name)
        val dataObject = getSerialisedVPass(viewModel.Vpasses.value!![position])
        model.barcodeToDisplay.value = dataObject
        model.getShowingBarcodeInScannedBarcodeFragment().value = true
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container,
                ScannedBarcodeFragment(),
                "show_scan_result"
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("show_scan_result")
            .commit()
    }

    override fun onVPassDelete(vpass: VPassData) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setPositiveButton(getString(R.string.delete)) { _, _ ->
            Log.e("TAG", "delete: ${vpass}")
            viewModel.deleteVPass(vpass)
            Toast.makeText(activity, "Vaccine deleted", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            Toast.makeText(context, "Deletion canceled", Toast.LENGTH_SHORT).show()
        }
        builder.setMessage(getString(R.string.confirm_delete_msg))
        builder.show()
    }

    override fun getPreferences(modePrivate: Int): Any {
        TODO("Not yet implemented")
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