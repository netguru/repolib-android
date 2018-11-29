package co.netguru.datasource

import co.netguru.data.Query
import co.netguru.data.RequestType
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class SimpleDataSourceController<E>(private val dataSource: DataSource<E>) : DataSourceController<E> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<E>()

    override fun dataOutput(): Flowable<E> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    override fun fetch(query: Query<E>): Completable = RequestType.FETCH.createRequest(query)
            .flatMap { request -> dataSource.fetch(request) }
            .map {
                dataOutputBehaviourSubject.onNext(it)
                it
            }.doOnError { dataOutputBehaviourSubject.onError(it) }
            .flatMapCompletable { Completable.complete() }

    override fun create(entity: E): Completable = RequestType.CREATE.createRequest(entity = entity)
            .flatMapCompletable { request -> dataSource.create(request) }

    override fun update(entity: E): Completable = RequestType.UPDATE.createRequest(entity = entity)
            .flatMapCompletable { request -> dataSource.update(request) }

    override fun delete(query: Query<E>): Completable = RequestType.DELETE
            .createRequest(query = query)
            .flatMapCompletable { dataSource.delete(it) }
}
