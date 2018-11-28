package co.netguru.datasource

import io.reactivex.Flowable

fun <T> DataSourceController<T>.asFlowable(): Flowable<DataSourceController<T>> = Flowable.just(this)

fun <T> DataSourceController<T>.applyAdditionalAction(modifier: (DataSourceController<T>) -> Flowable<T>)
        : Flowable<T> = this.asFlowable().flatMap(modifier)
