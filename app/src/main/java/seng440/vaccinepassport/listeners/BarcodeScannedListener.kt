package seng440.vaccinepassport.listeners

interface BarcodeScannedListener {
    fun onScanned(rawData: ByteArray)
}