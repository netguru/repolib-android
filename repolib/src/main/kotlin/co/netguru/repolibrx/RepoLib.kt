package co.netguru.repolibrx

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType.*
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.RequestsStrategy
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val requestsStrategy: RequestsStrategy
) : RepoLibRx<T> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    override fun outputDataStream(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    /**
     * Output stream that is responsible for transmitting data from the data sources.
     * Data emission is triggered by the input event sent using one of the input methods
     * e.g. [fetch].
     * [<br><br>]
     * Source for the data emission is selected by the RequestsStrategy object
     *
     */
    override fun fetch(query: Query<T>): Completable = handleRequest(
            Request(
                    type = FETCH,
                    query = query
            )
    )

    override fun create(entity: T): Completable = handleRequest(
            Request(
                    type = CREATE,
                    entity = entity
            )
    )

    override fun update(entity: T): Completable = handleRequest(
            Request(
                    type = UPDATE,
                    entity = entity
            )
    )

    override fun delete(query: Query<T>): Completable = handleRequest(
            Request(
                    type = DELETE,
                    query = query
            )
    )

    private fun handleRequest(request: Request<T>): Completable {
        return requestsStrategy.select(request)
                .apply(localDataSource, remoteDataSource, selectAction(request))
                .doOnNext {
                    dataOutputBehaviourSubject.onNext(it)
                }.ignoreElements()
    }

    private fun selectAction(request: Request<T>)
            : (DataSource<T>) -> Observable<T> = when (request.type) {
        CREATE -> { dataSource -> dataSource.create(request) }
        DELETE -> { dataSource -> dataSource.delete(request) }
        UPDATE -> { dataSource -> dataSource.update(request) }
        FETCH -> { dataSource -> dataSource.fetch(request) }
    }
}