package co.netguru.repolib.feature.demo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.netguru.repolib.feature.demo.DemoViewModel
import co.netguru.repolibrx.RepoLibRx
import javax.inject.Inject

class ViewModelFactory @Inject constructor(private val repoLibRx: RepoLibRx<DataEntity>) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DemoViewModel(repoLibRx) as T
    }
}