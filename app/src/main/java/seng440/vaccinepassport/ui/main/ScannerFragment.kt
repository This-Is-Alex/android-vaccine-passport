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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import seng440.vaccinepassport.BarcodeImageAnalyser
import seng440.vaccinepassport.R
import seng440.vaccinepassport.listeners.BarcodeScannedListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScannerFragment : Fragment() {

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

    override fun onStart() {
        super.onStart()
        model.getActionBarTitle().value = getString(R.string.scanner_actionbar_title)
        model.getActionBarSubtitle().value = getString(R.string.scanner_actionbar_subtitle)

        if (!cameraState && hasPermission) {
            cameraState = true
            initCamera()
        }
    }

    override fun onStop() {
        super.onStop()
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

                val analyser = BarcodeImageAnalyser(requireActivity() as BarcodeScannedListener)
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

}