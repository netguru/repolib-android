package co.netguru

import io.reactivex.Completable
import io.reactivex.Flowable

interface DataSource<T> {
    fun dataStream(): Flowable<T>

    fun update(entity: T): Completable
}