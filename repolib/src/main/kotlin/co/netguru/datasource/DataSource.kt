package co.netguru.datasource

import co.netguru.data.Request
import io.reactivex.Observable

interface DataSource<E> {

    fun create(request: Request<E>): Observable<E>

    fun delete(request: Request<E>): Observable<E>

    fun fetch(request: Request<E>): Observable<E>

    fun update(request: Request<E>): Observable<E>
}