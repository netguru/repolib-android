package co.netguru.strategy

import io.reactivex.Flowable

abstract class Resource<T> {

    abstract fun flowable(): Flowable<T>
}