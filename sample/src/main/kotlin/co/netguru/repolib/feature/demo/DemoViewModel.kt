package co.netguru.repolib.feature.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.ViewUpdateState
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryWithParams
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class DemoViewModel @Inject constructor(
        private val repoLibRx: RepoLibRx<DemoDataEntity>
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val onComplete: () -> Unit = { stateLiveData.postValue(ViewUpdateState()) }
    private val onError: (Throwable) -> Unit = {
        Timber.e(it)
        stateLiveData.postValue(ViewUpdateState(it.message))
    }

    val updatedItemLiveData = MutableLiveData<DemoDataEntity>()
    val removedItemLiveData = MutableLiveData<DemoDataEntity>()
    val stateLiveData = MutableLiveData<ViewUpdateState>()
    val editDataLiveData = MutableLiveData<DemoDataEntity>()

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun refresh() = handleRequest(repoLibRx.fetch(QueryAll))
    fun addNew(text: String) = handleRequest(repoLibRx.create(DemoDataEntity(-1, text)))

    fun getData(onItemReceived: (DemoDataEntity) -> Unit) {
        compositeDisposable += repoLibRx.outputDataStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = onItemReceived, onError = onError)
    }

    fun setupUpdatingAction(updateSubject: PublishSubject<DemoDataEntity>) {
        compositeDisposable += updateSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { itemToEdit -> editDataLiveData.postValue(itemToEdit) }
                .onErrorResumeNext(Observable.empty())
                .subscribe()
    }

    fun setupRemovingAction(removeSubject: PublishSubject<DemoDataEntity>) {
        compositeDisposable += removeSubject
                .flatMap { itemToDelete ->
                    repoLibRx.delete(QueryWithParams("id" to itemToDelete.id))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete { removedItemLiveData.postValue(itemToDelete) }
                            .doOnError(onError)
                            .onErrorComplete()
                            .andThen(Observable.just(itemToDelete))
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun handleRequest(requestCompletable: Completable) {
        requestCompletable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = onComplete, onError = onError)
    }
}