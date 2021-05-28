package seng440.vaccinepassport.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import seng440.vaccinepassport.BarcodeImageAnalyser
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass
import seng440.vaccinepassport.VaccineType
import seng440.vaccinepassport.listeners.BarcodeScannedListener
import seng440.vaccinepassport.room.VPassLiveRoomApplication
import seng440.vaccinepassport.room.VPassViewModel
import seng440.vaccinepassport.room.VPassViewModelFactory
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScannerFragment : Fragment(), BarcodeScannedListener {
    private var cameraExecutor: ExecutorService? = null
    private var cameraState: Boolean = false
    private var hasPermission: Boolean = false

    companion object {
        fun newInstance() = ScannerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permission = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else if (permission == PackageManager.PERMISSION_DENIED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(R.string.why_camera_permission)
                    .setPositiveButton(R.string.why_camera_permission,
                        DialogInterface.OnClickListener { _, _ ->
                            askCameraPermission()
                        })
                builder.create()
            } else {
                askCameraPermission()
            }
        }
    }

    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onResume() {
        super.onResume()
        model.getActionBarTitle().value = getString(R.string.scanner_actionbar_title)
        model.getActionBarSubtitle().value = getString(R.string.scanner_actionbar_subtitle)
        model.gethideHeader().value = false

        if (!cameraState && hasPermission) {
            cameraState = true
            initCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraState = false
        cameraExecutor?.shutdown()
    }

    private fun askCameraPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    hasPermission = true
                    initCamera()
                } else {
                    // Permission was denied
                    requireActivity().supportFragmentManager.popBackStackImmediate()
                }
            }

        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA)
    }

    private fun initCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                    requireView().findViewById<PreviewView>(R.id.viewFinder).createSurfaceProvider()
                )
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()

                val analyser = BarcodeImageAnalyser(this as BarcodeScannedListener)
                val imageAnalysis = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor!!, analyser)
                    }
                 // Stops using the camera when the fragment stops, don't have to worry about resource usage
                 cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("ScannerFragment", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun timestampToDate(timestamp: Int): Date {
        return Date(timestamp.toLong() * 86400L * 1000L)
    }

    private fun isLettersOrDigits(chars: String): Boolean {
        for (c in chars) {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }

    override fun onScanned(rawData: ByteArray) {
        val inputStream: DataInputStream = DataInputStream(ByteArrayInputStream(rawData))

        val dateAdministered = inputStream.readInt()
        val vaccineType = VaccineType.fromId(inputStream.readByte())
        val dosageNumber = inputStream.readByte().toInt()

        val passportNumberRaw = ByteArray(9)
        inputStream.read(passportNumberRaw, 0, 9)

        val passportNumber = String(passportNumberRaw)
                .trimEnd(Integer.valueOf(0).toChar()) //trim 0s off the end
        val passportExpiry = inputStream.readInt()
        val dateOfBirth = inputStream.readInt()

        val countryRaw = ByteArray(3)
        inputStream.read(countryRaw, 0, 3)

        val country = String(countryRaw)

        Log.d("Barcode", "About to read names... $dateAdministered ${vaccineType?.fullName} $dosageNumber $passportNumber")

        val rawName = ByteArray(inputStream.readByte().toInt())
        inputStream.read(rawName, 0, rawName.size)
        val rawDoctor = ByteArray(inputStream.readByte().toInt())
        inputStream.read(rawDoctor, 0, rawDoctor.size)

        val name = String(rawName)
        val doctorName = String(rawDoctor)

        Log.d("Barcode", "Names are $name, $doctorName")

        if (inputStream.read() != -1) return //there is still more data, must be the wrong format
        if (vaccineType == null || !isLettersOrDigits(passportNumber) || !isLettersOrDigits(country)) return

        Log.d("Barcode", "Read successfully")

        val dataObject = SerializableVPass(dateAdministered, vaccineType.id, doctorName, dosageNumber.toShort(), name, passportNumber, passportExpiry, dateOfBirth, country)
        model.barcodeToDisplay.value = dataObject

        model.getShowingBarcodeInScannedBarcodeFragment().value = false
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container,
                ScannedBarcodeFragment(),
                "show_scan_result"
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack("show_scan_result")
            .commit()
    }

}