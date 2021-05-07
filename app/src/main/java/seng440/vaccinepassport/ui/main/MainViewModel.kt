package seng440.vaccinepassport.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val actionBarTitle: MutableLiveData<String> = MutableLiveData("")
    private val actionBarSubtitle: MutableLiveData<String> = MutableLiveData("")

    fun getActionBarTitle(): MutableLiveData<String> {
        return actionBarTitle
    }

    fun getActionBarSubtitle(): MutableLiveData<String> {
        return actionBarSubtitle
    }
}