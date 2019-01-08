package co.netguru.repolibrx.datasource

import co.netguru.repolibrx.data.Query
import io.reactivex.Observable

interface DataSource<E> {
    fun create(entity: E): Observable<E>
    fun update(entity: E): Observable<E>
    fun delete(query: Query): Observable<E>
    fun fetch(query: Query): Observable<E>
}