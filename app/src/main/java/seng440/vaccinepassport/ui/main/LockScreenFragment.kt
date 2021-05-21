package seng440.vaccinepassport.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import seng440.vaccinepassport.R

class LockScreenFragment : Fragment() {

    companion object {
        fun newInstance() = LockScreenFragment()
    }

    private lateinit var pinDisplay: TextView
    private var typedPass = ""
    private lateinit var pin: String

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.lock_screen_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        pinDisplay = view.findViewById(R.id.pinDisplay)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        pin = sharedPreferences.getString("pin", "0000")!!

        view.findViewById<Button>(R.id.lockButton1)?.setOnClickListener {
            numPress("1")
        }
        view.findViewById<Button>(R.id.lockButton2)?.setOnClickListener {
            numPress("2")
        }
        view.findViewById<Button>(R.id.lockButton3)?.setOnClickListener {
            numPress("3")
        }
        view.findViewById<Button>(R.id.lockButton4)?.setOnClickListener {
            numPress("4")
        }
        view.findViewById<Button>(R.id.lockButton5)?.setOnClickListener {
            numPress("5")
        }
        view.findViewById<Button>(R.id.lockButton6)?.setOnClickListener {
            numPress("6")
        }
        view.findViewById<Button>(R.id.lockButton7)?.setOnClickListener {
            numPress("7")
        }
        view.findViewById<Button>(R.id.lockButton8)?.setOnClickListener {
            numPress("8")
        }
        view.findViewById<Button>(R.id.lockButton9)?.setOnClickListener {
            numPress("9")
        }
        view.findViewById<Button>(R.id.lockButton0)?.setOnClickListener {
            numPress("0")
        }

        view.findViewById<Button>(R.id.lockButtonDelete)?.setOnClickListener {
            numPress("delete")
        }

        view.findViewById<Button>(R.id.lockButtonConfirm)?.setOnClickListener {
            if (typedPass == pin) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container,
                        MainFragment()
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("lock_screen")
                    .commit()
            } else {
                typedPass = ""
                pinDisplay.text = ""
                view.findViewById<TextView>(R.id.pinErrorText)?.text = getString(R.string.bad_pin_msg)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.app_name)
        model.getActionBarSubtitle().value = ""
        model.gethideHeader().value = true
    }

    fun numPress(num: String) {
        val display = pinDisplay.text.toString()
        if (num == "delete") {
            if (display != null && display.length > 0) {
                pinDisplay.text = display.substring(0, display.length - 1)
                typedPass = typedPass.substring(0, display.length - 1)
            }
        } else {
            typedPass += num
            pinDisplay.text = display + "*"
        }
    }

}