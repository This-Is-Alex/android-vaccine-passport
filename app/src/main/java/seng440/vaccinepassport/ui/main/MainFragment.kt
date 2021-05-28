package seng440.vaccinepassport.ui.main

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import seng440.vaccinepassport.*
import seng440.vaccinepassport.receivers.BootReceiver
import seng440.vaccinepassport.room.*

class MainFragment : Fragment(), VPassAdapter.OnVPassListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: VPassViewModel by activityViewModels() {
        VPassViewModelFactory((activity?.application as VPassLiveRoomApplication).repository)
    }

    private val model: MainViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view  = inflater.inflate(R.layout.main_fragment, container, false)
        val adapter = VPassAdapter(listOf(), this)
        viewModel.Vpasses.observe(viewLifecycleOwner) { newPasses ->
            adapter.setData(newPasses)
        }
        val recycler : RecyclerView = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter
        createNotificationChannel()
        Utilities.scheduleReminder(this.requireContext(), 11, 50)

        val receiver = ComponentName(this.requireContext(), BootReceiver::class.java)
        this.requireContext().packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        return view
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.app_name)
        model.getActionBarSubtitle().value = ""
        model.gethideHeader().value = false
    }

    override fun onVPassClick(position: Int) {
        Log.i("CLICK", "Displaying Data for " + viewModel.Vpasses.value!![position].name)
        val dataObject = getSerialisedVPass(viewModel.Vpasses.value!![position])
        requireActivity().intent.putExtra("vaccineData", dataObject)
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
        Log.e("TAG", "delete: ${vpass}")
        viewModel.deleteVPass(vpass)
        Toast.makeText(activity, "Vaccine deleted", Toast.LENGTH_SHORT).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Notification.CATEGORY_REMINDER, "Daily Reminders", importance).apply {
            description = "Send daily reminders to capture memories"
        }
        val notificationManager: NotificationManager = getSystemService(this.requireContext(), NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}