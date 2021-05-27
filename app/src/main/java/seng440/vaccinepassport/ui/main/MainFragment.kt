package seng440.vaccinepassport.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
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
        val adapter = VPassAdapter(listOf(), this)
        viewModel.Vpasses.observe(viewLifecycleOwner, { newPasses ->
            adapter.setData(newPasses)
        })
        val recycler : RecyclerView = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter
        return view
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.app_name)
        model.getActionBarSubtitle().value = ""
        model.gethideHeader().value = false
    }

    override fun onVPassClick(position: Int) {
        Log.e("Tag", "clicked")
        TODO("Not yet implemented")
    }

    override fun onVPassDelete(vpass: VPassData) {
        Log.e("TAG", "delete: ${vpass}")
        viewModel.deleteVPass(vpass)
        Toast.makeText(activity, "Vaccine deleted", Toast.LENGTH_SHORT).show()
    }

    override fun getPreferences(modePrivate: Int): Any {
        TODO("Not yet implemented")
    }


}