package seng440.vaccinepassport.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
import seng440.vaccinepassport.room.VPassAdapter
import seng440.vaccinepassport.room.VPassLiveRoomApplication
import seng440.vaccinepassport.room.VPassViewModel
import seng440.vaccinepassport.room.VPassViewModelFactory

class MainFragment : Fragment() {

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
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        val vaccines = viewModel.
        vaccines.reverse() // So the most recent appears first
        val adapter = VPassAdapter(view.context, vaccines)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter
        return view
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.app_name)
        model.getActionBarSubtitle().value = ""
    }

}