package co.netguru.data

data class Request<T>(
        val type: RequestType,
        val entity: T? = null,
        val query: Query<T>? = null
)