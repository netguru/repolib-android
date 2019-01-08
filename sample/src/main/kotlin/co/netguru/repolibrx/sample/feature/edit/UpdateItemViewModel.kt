package co.netguru.repolibrx.sample.feature.edit

import androidx.lifecycle.ViewModel
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class UpdateItemViewModel @Inject constructor(
        private val repoLibRx: RepoLibRx<DemoDataEntity>
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    fun update(
            itemToUpdate: DemoDataEntity,
            onCompleteAction: () -> Unit,
            onError: (Throwable) -> Unit
    ) {
        compositeDisposable += repoLibRx.update(itemToUpdate).subscribe(onCompleteAction, onError)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}