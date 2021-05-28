package seng440.vaccinepassport.ui.main

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import seng440.vaccinepassport.R
import seng440.vaccinepassport.SerializableVPass

class MainViewModel : ViewModel() {

    private val actionBarTitle: MutableLiveData<String> = MutableLiveData("")
    private val actionBarSubtitle: MutableLiveData<String> = MutableLiveData("")
    private val hideHeader: MutableLiveData<Boolean> = MutableLiveData(false)

    private val showBarcodeInScannedBarcodeFragment: MutableLiveData<Boolean> = MutableLiveData(false)
    final val showBottomNavBar: MutableLiveData<Boolean> = MutableLiveData(true)
    final val barcodeToDisplay: MutableLiveData<SerializableVPass?> = MutableLiveData(null)

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