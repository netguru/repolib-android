package co.netguru.repolib.feature.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.ViewData
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.Query
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

    init {
        compositeDisposable += repoLibRx.outputDataStream()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.d("item added")
                            items.addOrUpdate(it)
                            viewLiveData.postValue(ViewData(items = items))
                        },
                        onError = {
                            Timber.e(it)
                            viewLiveData.postValue(ViewData(error = it.message, items = items))
                        }
                )

        compositeDisposable += removeSubject.doOnNext { Timber.d("removing... $it") }
                .flatMap { itemToDelete ->
                    repoLibRx.delete(object : Query<DemoDataEntity>(itemToDelete) {})
                            .doOnComplete {
                                Timber.d("removed")
                                viewLiveData.postValue(ViewData(items = items))
                            }
                            .doOnError {
                                Timber.e(it)
                                viewLiveData.postValue(ViewData(it.message, items))
                            }.andThen(Observable.just(itemToDelete))
                            .onErrorResumeNext(Observable.just(itemToDelete))
                }.subscribe()

        compositeDisposable += updateSubject
                .doOnNext { itemToEdit -> editDataLiveData.postValue(itemToEdit) }
                .onErrorResumeNext(Observable.empty())
                .subscribe()
    }

    fun data(): LiveData<ViewData> = viewLiveData

    fun dataToEdit(): LiveData<DemoDataEntity> = editDataLiveData

    fun refresh() {
        items.clear()
        Timber.d("refreshing...")
        compositeDisposable += repoLibRx.fetch(query = query)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            Timber.d("refreshed")
                            viewLiveData.postValue(ViewData(items = items))
                        },
                        onError = {
                            Timber.e(it)
                            viewLiveData.postValue(ViewData(it.message, items))
                        }
                )
    }

    fun addNew(text: String) {
        Timber.d("creating...")
        compositeDisposable += repoLibRx.create(DemoDataEntity(-1, text))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            Timber.d("created")
                            viewLiveData.postValue(ViewData(items = items))
                        },
                        onError = {
                            Timber.e(it)
                            viewLiveData.postValue(ViewData(it.message, items))
                        }
                )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun removeSubject(): PublishSubject<DemoDataEntity> = removeSubject
    fun updateSubject(): PublishSubject<DemoDataEntity> = updateSubject
}
