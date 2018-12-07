package co.netguru.queue

import co.netguru.data.Request

interface RequestQueue<E> {
    fun remove(request: Request<E>)

    fun add(request: Request<E>)

    fun getAllRequests(): List<Request<E>>

    fun isEmpty(): Boolean
}