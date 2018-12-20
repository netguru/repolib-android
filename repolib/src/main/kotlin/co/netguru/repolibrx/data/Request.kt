package co.netguru.repolibrx.data

data class Request<T>(
        val type: RequestType,
        val entity: T? = null,
        val query: Query<T>? = null
)