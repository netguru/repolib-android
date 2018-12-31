package co.netguru.repolib.feature.edit

import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.RepoLibRx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class UpdateItemViewModel @Inject constructor(private val repoLibRx: RepoLibRx<DemoDataEntity>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    fun update(itemToUpdate: DemoDataEntity, onCompleteAction: () -> Unit) {
        compositeDisposable += repoLibRx.update(itemToUpdate).subscribe(onCompleteAction)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}