package co.netguru.repolib.feature.demo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.ViewUpdateState
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.Query
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

class DemoViewModel @Inject constructor(private val repoLibRx: RepoLibRx<DemoDataEntity>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    //    private val items = mutableListOf<DemoDataEntity>()
    private val query = object : Query<DemoDataEntity>() {}
    private val removeSubject = PublishSubject.create<DemoDataEntity>()
    private val updateSubject = PublishSubject.create<DemoDataEntity>()

    private val onComplete: () -> Unit = { stateLiveData.postValue(ViewUpdateState()) }
    private val onError: (Throwable) -> Unit = {
        Timber.e(it)
        stateLiveData.postValue(ViewUpdateState(it.message))
    }

    val updatedItemLiveData = MutableLiveData<DemoDataEntity>()
    val removedItemLiveData = MutableLiveData<DemoDataEntity>()
    val stateLiveData = MutableLiveData<ViewUpdateState>()
    val editDataLiveData = MutableLiveData<DemoDataEntity>()

    init {
        setupRemovingAction()
        setupUpdatingAction()
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun removeSubject(): PublishSubject<DemoDataEntity> = removeSubject
    fun updateSubject(): PublishSubject<DemoDataEntity> = updateSubject
    fun refresh() = handleRequest(repoLibRx.fetch(query = query))
    fun addNew(text: String) = handleRequest(repoLibRx.create(DemoDataEntity(-1, text)))

    private fun handleRequest(requestCompletable: Completable) {
        requestCompletable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = onComplete, onError = onError)
    }

    private fun setupRemovingAction() {
        compositeDisposable += removeSubject.doOnNext { Timber.d("removing... $it") }
                .flatMap { itemToDelete ->
                    repoLibRx.delete(object : Query<DemoDataEntity>(itemToDelete) {})
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnComplete { removedItemLiveData.postValue(itemToDelete) }
                            .doOnError(onError)
                            .onErrorComplete()
                            .andThen(Observable.just(itemToDelete))
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun setupUpdatingAction() {
        compositeDisposable += updateSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { itemToEdit -> editDataLiveData.postValue(itemToEdit) }
                .onErrorResumeNext(Observable.empty())
                .subscribe()
    }

    fun getData(onItemReceived: (DemoDataEntity) -> Unit) {
        compositeDisposable += repoLibRx.outputDataStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = onItemReceived, onError = onError)
    }
}