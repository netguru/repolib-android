package com.netguru.repolibrx.data

import com.netguru.repolibrx.RepoLibRx
import com.netguru.repolibrx.datasource.DataSource

/**
 * Request class represents requests that are generated by the input methods in [RepoLibRx] like:
 * [RepoLibRx.create], [RepoLibRx.fetch], [RepoLibRx.update], [RepoLibRx.delete]. It wraps the parameters
 * required by the [DataSource] interface methods and allows to handle all requested in unified way.
 *
 */
sealed class Request<T> {
    data class Create<T>(val entity: T) : Request<T>()
    data class Update<T>(val entity: T) : Request<T>()
    data class Delete<T>(val query: Query) : Request<T>()
    data class Fetch<T>(val query: Query) : Request<T>()
}