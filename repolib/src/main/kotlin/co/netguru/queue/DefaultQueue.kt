package co.netguru.queue

import co.netguru.data.Request

class DefaultQueue<T>(
        private val mapQueue: MutableMap<Int, Request<T>> = mutableMapOf()
) : RequestQueue<T> {

    override fun remove(request: Request<T>) {
        mapQueue.remove(prepareMapKey(request))
    }

    override fun add(request: Request<T>) {
        mapQueue[prepareMapKey(request)] = request
    }

    override fun getAllRequests(): List<Request<T>> = mapQueue.values.toList()

    override fun isEmpty(): Boolean = mapQueue.isEmpty()

    private fun prepareMapKey(request: Request<T>): Int = request.hashCode()
}

