package seng440.vaccinepassport.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import seng440.vaccinepassport.R

class LockScreenFragment : Fragment() {

    companion object {
        fun newInstance() = LockScreenFragment()
    }

    private val model: MainViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.lock_screen_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.app_name)
        model.getActionBarSubtitle().value = ""
    }

}