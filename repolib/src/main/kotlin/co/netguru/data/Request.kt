package co.netguru.data

data class Request<T>(
        val requestType: RequestType,
        val entity: T? = null,
        val query: Query<T>? = null
)