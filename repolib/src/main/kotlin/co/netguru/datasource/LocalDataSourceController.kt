package co.netguru.datasource

import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class LocalDataSourceController<E>(private val dataSource: DataSource<E>) : DataSourceController<E> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<E>()

    override fun dataOutput(): Flowable<E> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    override fun fetch(query: Query<E>): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.FETCH,
                query = query
        )
    }.flatMap { request -> dataSource.fetch(request) }
            .doOnNext {
                dataOutputBehaviourSubject.onNext(it)
            }.doOnError { dataOutputBehaviourSubject.onError(it) }
            .ignoreElements()

    override fun create(entity: E): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.CREATE,
                entity = entity
        )
    }.flatMapCompletable { request -> dataSource.create(request) }

    override fun update(entity: E): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.UPDATE,
                entity = entity
        )
    }.flatMapCompletable { request -> dataSource.update(request) }

    override fun delete(query: Query<E>): Completable = Flowable.fromCallable {
        Request(
                requestType = RequestType.DELETE,
                query = query
        )
    }.flatMapCompletable { dataSource.delete(it) }
}
