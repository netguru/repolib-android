package co.netguru.repolib.feature.demo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.netguru.repolib.feature.demo.DemoViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.edit.UpdateItemViewModel
import co.netguru.repolibrx.RepoLibRx
import javax.inject.Inject

class DemoViewModelFactory @Inject constructor(
        private val repoLibRx: RepoLibRx<DemoDataEntity>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DemoViewModel(repoLibRx) as T
    }
}

class UpdateViewModelFactory @Inject constructor(private val repoLibRx: RepoLibRx<DemoDataEntity>) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UpdateItemViewModel(repoLibRx) as T
    }
}