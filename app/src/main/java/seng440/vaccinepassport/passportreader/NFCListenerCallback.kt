package seng440.vaccinepassport.passportreader

import android.nfc.Tag

interface NFCListenerCallback {
    fun onAvailableNFC(tag: Tag)
}