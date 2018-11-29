package co.netguru.cache

import co.netguru.data.Request

interface Cache<E> {
    fun remove(request: Request<E>)

    fun add(request: Request<E>)

    fun getAllRequests(): List<Request<E>>

    fun isEmpty(): Boolean
}