package seng440.vaccinepassport.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import seng440.vaccinepassport.R


class ScannerFragment : Fragment() {
    companion object {
        fun newInstance() = ScannerFragment()
    }

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.scanner_actionbar_title)
        model.getActionBarSubtitle().value = getString(R.string.scanner_actionbar_subtitle)
    }


}