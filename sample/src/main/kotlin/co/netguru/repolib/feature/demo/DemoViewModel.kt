package co.netguru.repolib.feature.demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.repolib.feature.demo.data.DataEntity
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.Query
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class DemoViewModel @Inject constructor(private val repoLibRx: RepoLibRx<DataEntity>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val items = mutableListOf<DataEntity>()
    private val liveData = MutableLiveData<List<DataEntity>>()
    private val query = object : Query<DataEntity> {}

    init {
        compositeDisposable += repoLibRx.outputDataStream()
                .subscribeBy(
                        onNext = {
                            items.add(it)
                        },
                        onError = {
                            Timber.e(it)
                        }
                )

    }

    fun data(): LiveData<List<DataEntity>> = liveData

    fun refresh() {
        compositeDisposable += repoLibRx.fetch(query = query)
                .subscribeBy(
                        onComplete = {
                            Timber.d("refreshed")
                            liveData.postValue(items)
                        },
                        onError = {
                            Timber.e(it)
                        }
                )
    }

    fun addNew(text: String) {
        compositeDisposable += repoLibRx.create(DataEntity(-1, text))
                .subscribeBy(
                        onComplete = {
                            Timber.d("created")
                            liveData.postValue(items)
                        },
                        onError = {
                            Timber.e(it)
                        }
                )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}