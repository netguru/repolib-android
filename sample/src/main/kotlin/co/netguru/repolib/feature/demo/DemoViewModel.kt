package co.netguru.repolib.feature.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DemoViewModel : ViewModel() {

    fun titlte(): LiveData<String> = MutableLiveData<String>().apply {
        value = "test"
    }
}