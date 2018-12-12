package co.netguru.datasource

import co.netguru.data.Request
import co.netguru.data.RequestType
import co.netguru.queue.RequestQueue
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable

class QueueDataSourceController<T>(
        private val dataSource: DataSource<T>,
        private val requestQueue: RequestQueue<T>) : DataSource<T> {

    override fun fetch(request: Request<T>): Flowable<T> = checkQueue()
            .andThen(dataSource.fetch(request))

    override fun delete(request: Request<T>): Completable = onRequest(request)

    override fun update(request: Request<T>): Completable = onRequest(request)

    override fun create(request: Request<T>): Completable = onRequest(request)

    private fun onRequest(
            request: Request<T>
    ): Completable = Completable.fromAction { requestQueue.add(request) }
            .andThen(executeRequestsQueue())

    private fun performRequest(
            request: Request<T>
    ): Completable = executeRequest(request)
            .doOnComplete {
                requestQueue.remove(request)
            }

    private fun executeRequest(request: Request<T>): Completable = when (request.type) {
        RequestType.CREATE -> dataSource.create(request)
        RequestType.DELETE -> dataSource.delete(request)
        RequestType.UPDATE -> dataSource.update(request)
        RequestType.FETCH -> Completable.error(
                IllegalStateException("FETCH request is not supported to be queued")
        )
    }

    private fun checkQueue(): Completable = if (requestQueue.isEmpty()) {
        Completable.complete()
    } else {
        executeRequestsQueue()
    }

    private fun executeRequestsQueue() = Observable
            .fromIterable(requestQueue.getAllRequests())
            .flatMapCompletable { performRequest(it) }
}
