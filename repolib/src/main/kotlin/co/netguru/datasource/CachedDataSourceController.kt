package co.netguru.datasource

import co.netguru.cache.Cache
import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class CachedDataSourceController<E>(
        private val cache: Cache<E>,
        private val dataSource: DataSource<E>,
        private val cachingValidator: CachingValidator) : DataSourceController<E> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<E>()

    override fun dataOutput(): Flowable<E> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    override fun fetch(query: Query<E>): Completable = RequestType.FETCH
            .createRequest(query = query)
            .flatMapCompletable { handleRequest(it) }

    override fun delete(query: Query<E>): Completable = RequestType.DELETE
            .createRequest(query = query)
            .flatMapCompletable { handleRequest(it) }

    override fun update(entity: E): Completable = RequestType.UPDATE
            .createRequest(entity = entity)
            .flatMapCompletable { handleRequest(it) }

    override fun create(entity: E): Completable = RequestType.CREATE
            .createRequest(entity = entity)
            .flatMapCompletable { handleRequest(it) }

    //Start
    private fun handleRequest(request: Request<E>)
            : Completable = if (cachingValidator.isOperationPermitted()) {
        checkCache(request)
    } else {
        Completable.fromAction { cache.add(request) }
    }

    private fun checkCache(request: Request<E>): Completable = if (cache.isEmpty()) {
        performRequest(request)
    } else {
        executeCachedRequests(request)
    }

    private fun executeCachedRequests(currentRequest: Request<E>): Completable = Observable
            .fromIterable(cache.getAllRequests())
            .mergeWith(Observable.fromCallable { currentRequest })
            .flatMapCompletable { performRequest(it) }

    private fun performRequest(request: Request<E>): Completable = execute(request)
            .doOnError {
                cache.add(request)
            }
            .doOnComplete {
                cache.remove(request)
            }

    private fun execute(request: Request<E>): Completable = when (request.requestType) {
        RequestType.CREATE -> dataSource.create(request)
        RequestType.DELETE -> dataSource.delete(request)
        RequestType.UPDATE -> dataSource.update(request)
        RequestType.FETCH -> dataSource.fetch(request)
                .doOnError { dataOutputBehaviourSubject.onError(it) }
                .doOnNext { dataOutputBehaviourSubject.onNext(it) }
                .flatMapCompletable { Completable.complete() }
    }
}
