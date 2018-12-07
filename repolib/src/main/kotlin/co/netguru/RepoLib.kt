package co.netguru

import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import co.netguru.datasource.DataSource
import co.netguru.strategy.SourcingStrategy
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val sourcingStrategy: SourcingStrategy
) {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    fun outputDataStream(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    /**
     * Output stream that is responsible for transmitting data from the data sources.
     * Data emission is triggered by the input event sent using one of the input methods
     * e.g. [fetch].
     * [<br><br>]
     * Source for the data emission is selected by the SourcingStrategy object
     *
     */
    fun fetch(query: Query<T>): Completable = Observable.fromCallable {
        Request(
                type = RequestType.FETCH,
                query = query
        )
    }.flatMap { request ->
        sourcingStrategy.select(request).apply(localDataSource, remoteDataSource) {
            it.fetch(request)
        }.toObservable()
    }.doOnNext {
        dataOutputBehaviourSubject.onNext(it)
    }.doOnError {
        dataOutputBehaviourSubject.onError(it)
    }.ignoreElements()


    fun create(entity: T): Completable = Flowable.fromCallable {
        Request(
                type = RequestType.CREATE,
                entity = entity
        )
    }.flatMap { request ->
        sourcingStrategy.select(request).apply(localDataSource, remoteDataSource) {
            it.create(request).toFlowable()
        }
    }.ignoreElements()

    fun update(entity: T): Completable = Flowable.fromCallable {
        Request(
                type = RequestType.UPDATE,
                entity = entity
        )
    }.flatMap { request ->
        sourcingStrategy.select(request).apply(localDataSource, remoteDataSource) {
            it.update(request).toFlowable()
        }
    }.ignoreElements()

    fun delete(query: Query<T>): Completable = Flowable.fromCallable {
        Request(
                type = RequestType.DELETE,
                query = query
        )
    }.flatMap { request ->
        sourcingStrategy.select(request).apply(localDataSource, remoteDataSource) {
            it.delete(request).toFlowable()
        }
    }.ignoreElements()
}