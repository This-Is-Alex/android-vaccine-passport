package seng440.vaccinepassport.room

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class VPassViewModel(private val vPassRepository: VPassRepository): ViewModel() {

    val Vpasses: LiveData<List<VPassData>> = vPassRepository.vPasses.asLiveData()
    val numVPasses: LiveData<Int> = vPassRepository.numVPasses.asLiveData()

    fun addVPass(VPass: VPassData) = viewModelScope.launch {
        vPassRepository.insert(VPass)
    }

    fun deleteVPass(VPass: VPassData) = viewModelScope.launch {
        vPassRepository.delete(VPass)
    }
}

class VPassViewModelFactory(private val repository: VPassRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VPassViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VPassViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}