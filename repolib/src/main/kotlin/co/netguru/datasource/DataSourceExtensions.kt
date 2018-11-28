package co.netguru.datasource

import io.reactivex.Flowable

fun <T> DataSource<T>.asFlowable(): Flowable<DataSource<T>> = Flowable.just(this)

fun <T> DataSource<T>.applyAdditionalAction(modifier: (DataSource<T>) -> Flowable<T>)
        : Flowable<T> = this.asFlowable().flatMap(modifier)