package co.netguru.repolibrx

import co.netguru.repolibrx.data.Query
import io.reactivex.Completable
import io.reactivex.Flowable

interface RepoLibRx<T> {

    fun outputDataStream(): Flowable<T>

    fun fetch(query: Query): Completable

    fun create(entity: T): Completable

    fun update(entity: T): Completable

    fun delete(query: Query): Completable
}