package co.netguru.repolibrx.sample.feature.demo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.sample.feature.demo.DemoViewModel
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import javax.inject.Inject

class ViewModelFactory @Inject constructor(private val repoLibRx: RepoLibRx<DemoDataEntity>)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DemoViewModel(repoLibRx) as T
    }
}