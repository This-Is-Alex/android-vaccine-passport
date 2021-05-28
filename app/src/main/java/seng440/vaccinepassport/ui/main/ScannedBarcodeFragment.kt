package seng440.vaccinepassport.ui.main

import android.app.PendingIntent
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.launch
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass
import seng440.vaccinepassport.VaccineType
import seng440.vaccinepassport.passportreader.IDPassport
import seng440.vaccinepassport.passportreader.NFCListenerCallback
import seng440.vaccinepassport.passportreader.PassportReaderCallback
import seng440.vaccinepassport.passportreader.PassportTask
import seng440.vaccinepassport.room.VPassData
import seng440.vaccinepassport.room.VPassLiveRoomApplication
import seng440.vaccinepassport.room.VPassViewModel
import seng440.vaccinepassport.room.VPassViewModelFactory
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.Charset
import java.security.Security
import java.text.SimpleDateFormat
import java.util.*


class ScannedBarcodeFragment : Fragment(), NFCListenerCallback, PassportReaderCallback {
    private val model: MainViewModel by activityViewModels()
    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy")
    private lateinit var passport: SerializableVPass
    private var readingPassport: Boolean = false
    private var nfc: Boolean = false
    private var displayingSavedData: Boolean = false

    private val viewModel: VPassViewModel by activityViewModels() {
        VPassViewModelFactory((activity?.application as VPassLiveRoomApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())

        displayingSavedData = false
        Log.d("Intent", "Just Scanned: " + requireActivity().intent.hasExtra("just_scanned"))
        Log.d("Intent", "Vaccine Data: " + requireActivity().intent.hasExtra("vaccineData"))
        if (requireActivity().intent.hasExtra("just_scanned")) {
            passport = requireActivity().intent.getSerializableExtra("just_scanned") as SerializableVPass
            requireActivity().intent.removeExtra("just_scanned")
        } else if (requireActivity().intent.hasExtra("vaccineData")) {
            Log.d("DATA", "Extracting data")
            passport = requireActivity().intent.getSerializableExtra("vaccineData") as SerializableVPass
            displayingSavedData = true
            requireActivity().intent.removeExtra("vaccineData")
        }
        nfc = model.getShowingBarcodeInScannedBarcodeFragment().value == false
        Log.d("DATA", "displayingSavedData = " + displayingSavedData.toString())
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

        if (nfc) {
            val nfcView = inflater.inflate(R.layout.layout_nfc_feedback, null, false)
            view.findViewById<CardView>(R.id.vpass_top_card).addView(nfcView)
        } else {
            val barcodeView = inflater.inflate(R.layout.layout_barcode_display, null, false)
            val imageView = barcodeView.findViewById<ImageView>(R.id.vpass_barcode)
            val image = generateBarcode(Resources.getSystem().displayMetrics.widthPixels)
            imageView.setImageBitmap(image)


            view.findViewById<CardView>(R.id.vpass_top_card).addView(barcodeView)
        }

        view.findViewById<CardView>(R.id.barcode_save_options).addView(setupSaveCancelView(inflater))

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isBorderMode: Boolean = sharedPreferences.getBoolean("border_mode", false)
        val isLogging: Boolean = sharedPreferences.getBoolean("logging_mode", false) && isBorderMode
        if (displayingSavedData) {
            view.findViewById<CardView>(R.id.barcode_save_options).addView(setupCloseView(inflater))
        } else if (!isBorderMode) {
            view.findViewById<CardView>(R.id.barcode_save_options).addView(setupSaveCancelView(inflater))
        } else if (isLogging) {
            view.findViewById<CardView>(R.id.barcode_save_options).addView(setupApproveRejectView(inflater))
        } else {    // Is border mode and not logging
            view.findViewById<CardView>(R.id.barcode_save_options).addView(setupDiscardView(inflater))
        }

        return view
    }

    private fun savePassportData(approved: Boolean = true) {
        val dataObject = VPassData(
                passport.date,
                passport.vacId,
                passport.drAdministered,
                passport.dosageNum,
                passport.name,
                passport.passportNum,
                passport.passportExpDate,
                passport.dob,
                passport.country,
                approved)
        viewModel.deleteVPass(dataObject)
        viewModel.addVPass(dataObject)
    }

    private fun setupSaveCancelView(inflater: LayoutInflater): View {
        val buttonMenuView = inflater.inflate(R.layout.layout_barcode_save_cancel, null, false)

        buttonMenuView.findViewById<Button>(R.id.save_barcode_btn).setOnClickListener(View.OnClickListener {
            savePassportData()

            Toast.makeText(context, "Vaccine passport saved", Toast.LENGTH_SHORT).show()
            goBack()
        })

        buttonMenuView.findViewById<Button>(R.id.cancel_barcode_btn).setOnClickListener(View.OnClickListener {
            goBack()
        })

        return buttonMenuView
    }

    private fun setupDiscardView(inflater: LayoutInflater): View {
        val buttonMenuView = inflater.inflate(R.layout.layout_barcode_discard, null, false)

        buttonMenuView.findViewById<Button>(R.id.discard_barcode_btn).setOnClickListener(View.OnClickListener {
            goBack()
        })

        return buttonMenuView
    }

    private fun setupCloseView(inflater: LayoutInflater): View {
        val buttonMenuView = inflater.inflate(R.layout.layout_barcode_close, null, false)

        buttonMenuView.findViewById<Button>(R.id.close_barcode_btn).setOnClickListener(View.OnClickListener {
            goBack()
        })

        return buttonMenuView
    }

    private fun setupApproveRejectView(inflater: LayoutInflater): View {
        val buttonMenuView = inflater.inflate(R.layout.layout_barcode_approve_reject, null, false)

        buttonMenuView.findViewById<Button>(R.id.approve_barcode_btn).setOnClickListener(View.OnClickListener {
            savePassportData(true)

            Toast.makeText(context, "Vaccine passport approved", Toast.LENGTH_SHORT).show()
            goBack()
        })

        buttonMenuView.findViewById<Button>(R.id.reject_barcode_btn).setOnClickListener(View.OnClickListener {
            savePassportData(false)

            Toast.makeText(context, "Vaccine passport rejected", Toast.LENGTH_SHORT).show()
            goBack()
        })

        return buttonMenuView
    }

    private fun goBack() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        val borderMode: Boolean = sharedPreferences.getBoolean("border_mode", false)
        val destination = if (borderMode) "scanner" else "main"
        Log.d("Navigate", "Closing barcode to ${destination}")

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

    override fun onResume() {
        super.onResume()
        if (nfc) {
            val adapter = NfcAdapter.getDefaultAdapter(requireContext())
            Log.d("Passport", "Firing up NFC")
            if (adapter != null) {
                if (!adapter.isEnabled) {
                    requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_disabled)
                    requireView().findViewById<CardView>(R.id.vpass_top_card).setBackgroundResource(R.color.nfc_none)
                } else {
                    if (!readingPassport) {
                        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text =
                            getString(R.string.nfc_ready)
                        requireView().findViewById<CardView>(R.id.vpass_top_card)
                            .setBackgroundResource(R.color.nfc_ready)
                    }
                    val intent = Intent(requireActivity(), requireActivity().javaClass)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    val pendingIntent =
                        PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    val filter = arrayOf(arrayOf("android.nfc.tech.IsoDep"))
                    adapter.enableForegroundDispatch(requireActivity(), pendingIntent, null, filter)
                }
            } else {
                requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_nonexistent)
                requireView().findViewById<TextView>(R.id.vpass_top_card).setBackgroundResource(R.color.nfc_none)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfc) {
            val adapter = NfcAdapter.getDefaultAdapter(requireContext())
            adapter?.disableForegroundDispatch(requireActivity())
        }
    }

    override fun onAvailableNFC(tag: Tag) {
        if (!nfc) return
        readingPassport = true
        Log.d("Passport", "Got tag")
        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_reading)
        requireView().findViewById<CardView>(R.id.vpass_top_card).setBackgroundResource(R.color.nfc_ready)
        lifecycleScope.launch {
            val task = PassportTask().readTag(tag, passport, requireContext(), getBarcodeCallback())
        }
    }

    private fun getBarcodeCallback(): PassportReaderCallback {
        return this
    }

    override fun onReadSuccess(passport: IDPassport) {
        if (!nfc) return
        readingPassport = false
        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_success)
            .replace("%NAME%", passport.fullName!!)
        requireView().findViewById<CardView>(R.id.vpass_top_card).setBackgroundResource(R.color.nfc_success)
    }

    override fun onReadFailure(msg: String) {
        if (!nfc) return
        readingPassport = false
        var message = msg
        if (message == "Tag was lost.") {
            message = getString(R.string.nfc_taglost_error)
        }
        requireView().findViewById<TextView>(R.id.vpass_nfc_status).text = getString(R.string.nfc_error)
            .replace("%REASON%", message)
        requireView().findViewById<CardView>(R.id.vpass_top_card).setBackgroundResource(R.color.nfc_fail)
    }

    private fun generateBarcode(size: Int): Bitmap? {
        val output = ByteArrayOutputStream()
        val dos = DataOutputStream(output)

        dos.writeInt(passport.date)
        dos.writeByte(passport.vacId.toInt())
        dos.writeByte(passport.dosageNum.toInt())
        dos.write(passport.passportNum.toByteArray(Charset.defaultCharset()))
        for (i in 1..(9 - passport.passportNum.length)) {
            dos.writeByte(0) //zero pad the passport num
        }
        dos.writeInt(passport.passportExpDate)
        dos.writeInt(passport.dob)
        dos.write(passport.country.toByteArray(Charset.defaultCharset()))
        dos.writeByte(passport.name.length)
        dos.write(passport.name.toByteArray(Charset.defaultCharset()))
        dos.writeByte(passport.drAdministered.length)
        dos.write(passport.drAdministered.toByteArray(Charset.defaultCharset()))

        dos.close()
        output.close()

        val data = String(output.toByteArray(), Charset.forName("ISO_8859-15"))
        Log.e("Data", data)
        Log.e("data", Arrays.toString(output.toByteArray()))
        Log.e("Data", output.toByteArray().joinToString("") { " 0x" + it.toString(16).padStart(2, '0') })

        val bitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.AZTEC, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (bitMatrix[j, i]) {
                    pixels[i * width + j] = -0x1000000
                } else {
                    pixels[i * width + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}