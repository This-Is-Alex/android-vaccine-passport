package seng440.vaccinepassport.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass
import seng440.vaccinepassport.listeners.BiometricAuthListener
import seng440.vaccinepassport.room.VPassData
import seng440.vaccinepassport.room.VPassLiveRoomApplication
import seng440.vaccinepassport.room.VPassViewModel
import seng440.vaccinepassport.room.VPassViewModelFactory

class LockScreenFragment : Fragment(), BiometricAuthListener {

    companion object {
        fun newInstance() = LockScreenFragment()
    }

    private val viewModel: VPassViewModel by viewModels() {
        VPassViewModelFactory((requireActivity().application as VPassLiveRoomApplication).repository)
    }

    private lateinit var pinDisplay: TextView
    private lateinit var pinErrorText: TextView
    private var typedPass = ""
    private lateinit var pin: String
    private lateinit var bioButton: Button

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.lock_screen_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        model.showBottomNavBar.value = false
        pinDisplay = view.findViewById(R.id.pinDisplay)
        pinErrorText = view.findViewById(R.id.pinErrorText)
        bioButton = view.findViewById(R.id.lockButtonFinger)

        showBiometricLoginOption() // Hides if not usable

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

        view.findViewById<Button>(R.id.lockButtonFinger)?.setOnClickListener {
            showBiometricPrompt(
                activity = requireActivity(),
                listener = this,
                cryptoObject = null,
                allowDeviceCredential = true
            )
        }

        view.findViewById<Button>(R.id.lockButtonConfirm)?.setOnClickListener {
            if (typedPass == pin) {
                success()
            } else {
                typedPass = ""
                pinDisplay.text = ""
                pinErrorText.text = getString(R.string.bad_pin_msg)
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
            if (display.length > 0) {
                pinDisplay.text = display.substring(0, display.length - 1)
                typedPass = typedPass.substring(0, display.length - 1)
            }
        } else {
            typedPass += num
            pinDisplay.text = display + "*"
        }
    }

    fun showBiometricLoginOption() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val use_bio = sharedPreferences.getBoolean("use_fingerprint", false)

        bioButton.visibility =
            if (isBiometricReady(requireContext()) && use_bio) View.VISIBLE
            else View.INVISIBLE
    }

    fun hasBiometricCapability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate()
    }

    fun isBiometricReady(context: Context) =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

    fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        allowDeviceCredential: Boolean
        ): BiometricPrompt.PromptInfo {
            val builder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)

        // Use Device Credentials if allowed, otherwise show Cancel Button
        builder.apply {
            if (allowDeviceCredential) setDeviceCredentialAllowed(true)
            else setNegativeButtonText("Cancel")
        }

        return builder.build()
    }

    fun initBiometricPrompt(
            activity: FragmentActivity,
            listener: BiometricAuthListener
        ): BiometricPrompt {
        // 1
        val executor = ContextCompat.getMainExecutor(activity)

        // 2
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticationError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(this.javaClass.simpleName, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticationSuccess(result)
            }
        }

        // 3
        return BiometricPrompt(activity, executor, callback)
    }

    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Enter biometric credentials to proceed.",
        description: String = "Input your Fingerprint or FaceID to ensure it's you!",
        activity: FragmentActivity,
        listener: BiometricAuthListener,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        allowDeviceCredential: Boolean = false
    ) {
        // 1
        val promptInfo = setBiometricPromptInfo(
            title,
            subtitle,
            description,
            allowDeviceCredential
        )

        // 2
        val biometricPrompt = initBiometricPrompt(activity, listener)

        // 3
        biometricPrompt.apply {
            if (cryptoObject == null) authenticate(promptInfo)
            else authenticate(promptInfo, cryptoObject)
        }
    }

    override fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult) {
        success()
    }

    private fun success() {
        model.showBottomNavBar.value = true

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .addToBackStack("main")
            .commit()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if ("android.intent.custom.scan" == requireActivity().intent.action) {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scan_now
        } else if ("android.intent.custom.show_latest" == requireActivity().intent.action) {
            showLatest()
        } else if (sharedPreferences.getBoolean("border_mode", false)) {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.menu_scan_now
        }
    }

    override fun onBiometricAuthenticationError(errorCode: Int, errorMessage: String) {
        typedPass = ""
        pinDisplay.text = ""
        pinErrorText.text = errorMessage
    }

    private fun showLatest() {
        Log.i("CLICK", "Displaying Data for latest")
        Log.i("CLICK", "Displaying Data for " + viewModel.Vpasses)

        lifecycleScope.launch {
            var dataObject: VPassData?
            withContext(Dispatchers.IO) {
                dataObject = try {
                    viewModel.getLatest()
                } catch (exception: Exception) {
                    null
                }
            }
            if (dataObject != null) {
                val cerealObject = getSerialisedVPass(dataObject!!)
                model.barcodeToDisplay.value = cerealObject
                model.getShowingBarcodeInScannedBarcodeFragment().value = true
                requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.container,
                                ScannedBarcodeFragment(),
                                "show_scan_result"
                        )
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("show_scan_result")
                        .commit()
            } else {
                makeToast(getString(R.string.no_pass_shortcut))
            }
        }
    }

    fun makeToast(string: String){
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
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