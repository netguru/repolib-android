package co.netguru.datasource

import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import io.reactivex.Flowable

fun <T> DataSourceController<T>.asFlowable(): Flowable<DataSourceController<T>> = Flowable.just(this)

fun <T> DataSourceController<T>.applyAdditionalAction(modifier: (DataSourceController<T>) -> Flowable<T>)
        : Flowable<T> = this.asFlowable().flatMap(modifier)

fun <T> RequestType.createRequest(
        query: Query<T>? = null,
        entity: T? = null
): Flowable<Request<T>> {
    return Flowable.fromCallable {
        Request(
                requestType = this,
                query = query,
                entity = entity

        )
    }
}