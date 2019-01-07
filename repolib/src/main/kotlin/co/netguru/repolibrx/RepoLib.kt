package co.netguru.repolibrx

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.RequestsStrategyFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val requestsStrategyFactory: RequestsStrategyFactory
) : RepoLibRx<T> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    override fun outputDataStream(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    /**
     * Output stream that is responsible for transmitting data from the data sources.
     * Data emission is triggered by the input event sent using one of the input methods
     * e.g. [fetch].
     * [<br><br>]
     * Source for the data emission is selected by the RequestsStrategyFactory object
     *
     */
    override fun fetch(query: Query<T>): Completable = handleRequest(Request.Fetch(query))

    override fun create(entity: T): Completable = handleRequest(Request.Create(entity))

    override fun update(entity: T): Completable = handleRequest(Request.Update(entity))

    override fun delete(query: Query<T>): Completable = handleRequest(Request.Delete(query))

    private fun handleRequest(request: Request<T>): Completable {
        return requestsStrategyFactory.select(request)
                .apply(localDataSource, remoteDataSource, selectAction(request))
                .doOnNext {
                    dataOutputBehaviourSubject.onNext(it)
                }.ignoreElements()
    }

    private fun selectAction(request: Request<T>)
            : (DataSource<T>) -> Observable<T> = when (request) {
        is Request.Create<T> -> { dataSource -> dataSource.create(request) }
        is Request.Delete<T> -> { dataSource -> dataSource.delete(request) }
        is Request.Update<T> -> { dataSource -> dataSource.update(request) }
        is Request.Fetch<T> -> { dataSource -> dataSource.fetch(request) }
    }
}