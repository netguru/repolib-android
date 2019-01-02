package co.netguru.repolib.feature.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.ViewData
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class DemoViewModel @Inject constructor(private val repoLibRx: RepoLibRx<DemoDataEntity>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val items = mutableListOf<DemoDataEntity>()
    private val viewLiveData = MutableLiveData<ViewData>()
    private val editDataLiveData = MutableLiveData<DemoDataEntity>()
    private val query = object : Query<DemoDataEntity>() {}
    private val removeSubject = PublishSubject.create<DemoDataEntity>()
    private val updateSubject = PublishSubject.create<DemoDataEntity>()
    private val onComplete: () -> Unit = {
        Timber.d("created")
        viewLiveData.postValue(ViewData(items = items))
    }
    private val onError: (Throwable) -> Unit = {
        Timber.e(it)
        viewLiveData.postValue(ViewData(it.message, items))
    }

    init {
        setupDataOutput()
        setupRemovingAction()
        setupUpdatingAction()

    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun data(): LiveData<ViewData> = viewLiveData
    fun removeSubject(): PublishSubject<DemoDataEntity> = removeSubject
    fun updateSubject(): PublishSubject<DemoDataEntity> = updateSubject

    fun dataToEdit(): LiveData<DemoDataEntity> = editDataLiveData

    fun refresh() {
        items.clear()
        handleRequest(repoLibRx.fetch(query = query))
    }

    fun addNew(text: String) {
        Timber.d("creating...")
        handleRequest(repoLibRx.create(DemoDataEntity(-1, text)))
    }

    private fun handleRequest(requestCompletable: Completable) {
        requestCompletable
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onComplete = onComplete, onError = onError)

    }

    private fun setupRemovingAction() {
        compositeDisposable += removeSubject.doOnNext { Timber.d("removing... $it") }
                .flatMap { itemToDelete ->
                    repoLibRx.delete(object : Query<DemoDataEntity>(itemToDelete) {})
                            .andThen(Observable.fromCallable {
                                items.indexOf(itemToDelete)
                            })
                            .doOnNext { viewLiveData.postValue(ViewData(items = items, indexToRemove = it)) }
                            .doOnError(onError)
                            .onErrorResumeNext(Observable.empty())
                }.subscribe()
    }

    private fun setupDataOutput() {
        compositeDisposable += repoLibRx.outputDataStream()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.d("item added")
                            items.addOrUpdate(it)
                            viewLiveData.postValue(ViewData(items = items))
                        },
                        onError = onError
                )
    }

    private fun setupUpdatingAction() {
        compositeDisposable += updateSubject
                .doOnNext { itemToEdit -> editDataLiveData.postValue(itemToEdit) }
                .onErrorResumeNext(Observable.empty())
                .subscribe()
    }
}