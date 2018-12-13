package co.netguru

import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType.*
import co.netguru.datasource.DataSource
import co.netguru.strategy.RequestsStrategy
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val requestsStrategy: RequestsStrategy
) {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    fun outputDataStream(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    /**
     * Output stream that is responsible for transmitting data from the data sources.
     * Data emission is triggered by the input event sent using one of the input methods
     * e.g. [fetch].
     * [<br><br>]
     * Source for the data emission is selected by the RequestsStrategy object
     *
     */
    fun fetch(query: Query<T>): Completable = handleRequest(
            Request(
                    type = FETCH,
                    query = query
            )
    )


    fun create(entity: T): Completable = handleRequest(
            Request(
                    type = CREATE,
                    entity = entity
            )
    )

    fun update(entity: T): Completable = handleRequest(
            Request(
                    type = UPDATE,
                    entity = entity
            )
    )

    fun delete(query: Query<T>): Completable = handleRequest(
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