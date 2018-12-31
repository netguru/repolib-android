package co.netguru.repolibrx.initializer

import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.DefaultRequestsStrategy
import co.netguru.repolibrx.strategy.RequestsStrategy
import kotlin.properties.Delegates

class RepoLibBuilder<T> {

    var localDataSourceController: DataSource<T> by Delegates.notNull()

    var remoteDataSourceController: DataSource<T> by Delegates.notNull()

    //    todo rename to factory
    var requestsStrategy: RequestsStrategy = DefaultRequestsStrategy()

    fun build(): RepoLibRx<T> {
        return RepoLib(
                localDataSource = localDataSourceController,
                remoteDataSource = remoteDataSourceController,
                requestsStrategy = requestsStrategy
        )
    }
}

fun <T> createRepo(init: RepoLibBuilder<T>.() -> Unit): RepoLibRx<T> {
    val builder = RepoLibBuilder<T>()
    init(builder)
    return builder.build()
}


