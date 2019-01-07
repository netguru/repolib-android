package co.netguru.repolibrx.data

sealed class Request<T> {
    data class Create<T>(val entity: T) : Request<T>()
    data class Update<T>(val entity: T) : Request<T>()
    data class Delete<T>(val query: Query) : Request<T>()
    data class Fetch<T>(val query: Query) : Request<T>()
}