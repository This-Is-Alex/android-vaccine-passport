package seng440.vaccinepassport.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val actionBarTitle: MutableLiveData<String> = MutableLiveData("")
    private val actionBarSubtitle: MutableLiveData<String> = MutableLiveData("")
    private val hideHeader: MutableLiveData<Boolean> = MutableLiveData(false)

    private val showBarcodeInScannedBarcodeFragment: MutableLiveData<Boolean> = MutableLiveData(false)

    fun getActionBarTitle(): MutableLiveData<String> {
        return actionBarTitle
    }

    fun getActionBarSubtitle(): MutableLiveData<String> {
        return actionBarSubtitle
    }

    fun gethideHeader(): MutableLiveData<Boolean> {
        return hideHeader
    }

    fun getShowingBarcodeInScannedBarcodeFragment(): MutableLiveData<Boolean> {
        return showBarcodeInScannedBarcodeFragment
    }
}