package seng440.vaccinepassport

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import seng440.vaccinepassport.listeners.BarcodeScannedListener

class BarcodeImageAnalyser(private val listener: BarcodeScannedListener) : ImageAnalysis.Analyzer {

    private val barcodeOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                    Barcode.FORMAT_AZTEC)
            .build()
    private val scanner = BarcodeScanning.getClient(barcodeOptions)
    private var foundBarcode = false

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val result = scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawBytes ?: continue
                        if (rawValue.size >= 28) {
                            val nameLength = rawValue[26].toInt()
                            if (rawValue.size >= (27 + nameLength)) {
                                val doctorLength = rawValue[27 + nameLength].toInt();
                                Log.d("Barcode", "Got ${rawValue.size}, name is $nameLength and doctor is $doctorLength")
                                if (rawValue.size == nameLength + doctorLength + 28) {
                                    foundBarcode = true
                                    listener.onScanned(rawValue)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
                .addOnCompleteListener{
                    if (!foundBarcode) {
                        imageProxy.close()
                    }
                }
        }
    }
}
