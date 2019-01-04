package co.netguru.repolibrx.datasource

import co.netguru.repolibrx.data.Request
import io.reactivex.Observable

interface DataSource<E> {

    fun create(request: Request.Create<E>): Observable<E>

    fun delete(request: Request.Delete<E>): Observable<E>

    fun fetch(request: Request.Fetch<E>): Observable<E>

    fun update(request: Request.Update<E>): Observable<E>
}