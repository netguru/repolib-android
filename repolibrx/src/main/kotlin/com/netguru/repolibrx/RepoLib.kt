package com.netguru.repolibrx

import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.Request
import com.netguru.repolibrx.datasource.DataSource
import com.netguru.repolibrx.strategy.RequestsStrategyFactory
import com.netguru.repolibrx.strategy.Strategy
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * [RepoLib] class is the **main controller** for managing DataSources synchronization.
 * Event synchronization is performed according to the [Strategy] produced by the [RequestsStrategyFactory].
 * [RequestsStrategyFactory] should be implemented manually by the developer according to the project requirements.
 *
 * @param localDataSource [DataSource] object that will be treated as local by the [Strategy]
 * @param remoteDataSource [DataSource] object that will be treated as remote by the [Strategy]
 * @param requestsStrategyFactory is an object of [RequestsStrategyFactory] that is used by the [RepoLib]
 * to retrieve [Strategy] for specific requests
 */
class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val requestsStrategyFactory: RequestsStrategyFactory
) : RepoLibRx<T> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    override fun outputDataStream(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    override fun fetch(query: Query): Completable = handleRequest(Request.Fetch(query))

    override fun create(entity: T): Completable = handleRequest(Request.Create(entity))

    override fun update(entity: T): Completable = handleRequest(Request.Update(entity))

    override fun delete(query: Query): Completable = handleRequest(Request.Delete(query))

    private fun handleRequest(request: Request<T>): Completable {
        return requestsStrategyFactory.select(request)
                .apply(localDataSource, remoteDataSource, selectAction(request))
                .doOnNext {
                    dataOutputBehaviourSubject.onNext(it)
                }.ignoreElements()
    }

    private fun selectAction(request: Request<T>)
            : (DataSource<T>) -> Observable<T> = when (request) {
        is Request.Create<T> -> { dataSource -> dataSource.create(request.entity) }
        is Request.Update<T> -> { dataSource -> dataSource.update(request.entity) }
        is Request.Delete<T> -> { dataSource -> dataSource.delete(request.query) }
        is Request.Fetch<T> -> { dataSource -> dataSource.fetch(request.query) }
    }

    companion object {
        /**
         * Value that can be used to set undefined id as data entity ID
         */
        const val UNDEFINED: Long = -1
    }
}