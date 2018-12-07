package co.netguru.datasource

import co.netguru.cache.RequestQueue
import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RemoteDataSourceController<T>(
        private val dataSource: DataSource<T>,
        private val requestQueue: RequestQueue<T>) : DataSourceController<T> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    override fun dataOutput(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    override fun fetch(query: Query<T>): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.FETCH,
                query = query
        )
    }.flatMapCompletable { executeRequest(it) }

    override fun delete(query: Query<T>): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.DELETE,
                query = query
        )
    }.flatMapCompletable { onRequest(it) }

    override fun update(entity: T): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.UPDATE,
                entity = entity
        )
    }.flatMapCompletable { onRequest(it) }

    override fun create(entity: T): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.CREATE,
                entity = entity
        )
    }.flatMapCompletable { onRequest(it) }

    private fun onFetchingError(error: Throwable): Flowable<T> = Flowable.error(error)

    private fun onRequest(
            request: Request<T>
    ): Completable = if (requestQueue.isEmpty()) {
        performRequest(request)
    } else {
        executeCachedRequests(request)
    }

    private fun executeCachedRequests(
            currentRequest: Request<T>
    ): Completable = Observable
            .fromIterable(requestQueue.getAllRequests())
            .mergeWith(Observable.fromCallable { currentRequest })
            .flatMapCompletable { request -> performRequest(request) }

    private fun performRequest(
            request: Request<T>
    ): Completable = executeRequest(request)
            .doOnError {
                requestQueue.add(request)
            }
            .doOnComplete {
                requestQueue.remove(request)
            }

    private fun executeRequest(request: Request<T>): Completable = when (request.requestType) {
        RequestType.CREATE -> dataSource.create(request)
        RequestType.DELETE -> dataSource.delete(request)
        RequestType.UPDATE -> dataSource.update(request)
        RequestType.FETCH -> fetchData(request)
                .doOnError { dataOutputBehaviourSubject.onError(it) }
                .doOnNext { dataOutputBehaviourSubject.onNext(it) }
                .flatMapCompletable { Completable.complete() }
    }

    private fun fetchData(request: Request<T>): Flowable<T> = dataSource.fetch(request)
            .onErrorResumeNext { error: Throwable ->
                onFetchingError(error)
            }
}
