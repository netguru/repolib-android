package co.netguru.repolib.feature.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.ViewData
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.Query
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class DemoViewModel @Inject constructor(private val repoLibRx: RepoLibRx<DemoDataEntity>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val items = mutableListOf<DemoDataEntity>()
    private val liveData = MutableLiveData<ViewData>()
    private val query = object : Query<DemoDataEntity> {}

    init {
        compositeDisposable += repoLibRx.outputDataStream()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.d("item added")
                            items.add(it)
                            liveData.postValue(ViewData(items = items))
                        },
                        onError = {
                            Timber.e(it)
                            liveData.postValue(ViewData(it.message, items))
                        }
                )

    }

    fun data(): LiveData<ViewData> = liveData

    fun refresh() {
        items.clear()
        compositeDisposable += repoLibRx.fetch(query = query)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            Timber.d("refreshed")
                            liveData.postValue(ViewData(items = items))
                        },
                        onError = {
                            Timber.e(it)
                            liveData.postValue(ViewData(it.message, items))
                        }
                )
    }

    fun addNew(text: String) {
        compositeDisposable += repoLibRx.create(DemoDataEntity(-1, text))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            Timber.d("created")
                            liveData.postValue(ViewData(items = items))
                        },
                        onError = {
                            Timber.e(it)
                            liveData.postValue(ViewData(it.message, items))
                        }
                )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}