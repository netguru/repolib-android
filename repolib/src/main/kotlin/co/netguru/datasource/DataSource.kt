package co.netguru.datasource

import co.netguru.data.Request
import io.reactivex.Completable
import io.reactivex.Flowable

interface DataSource<E> {

    fun create(request: Request<E>): Completable

    fun delete(request: Request<E>): Completable

    fun fetch(request: Request<E>): Flowable<E>

    fun update(request: Request<E>): Completable
}