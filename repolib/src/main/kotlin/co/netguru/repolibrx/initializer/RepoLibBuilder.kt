package co.netguru.repolibrx.initializer

import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.DefaultRequestsStrategyFactory
import co.netguru.repolibrx.strategy.RequestsStrategyFactory
import kotlin.properties.Delegates

class RepoLibBuilder<T> {

    var localDataSourceController: DataSource<T> by Delegates.notNull()

    var remoteDataSourceController: DataSource<T> by Delegates.notNull()

    var requestsStrategyFactory: RequestsStrategyFactory = DefaultRequestsStrategyFactory()

    fun build(): RepoLibRx<T> {
        return RepoLib(
                localDataSource = localDataSourceController,
                remoteDataSource = remoteDataSourceController,
                requestsStrategyFactory = requestsStrategyFactory
        )
    }
}

fun <T> createRepo(init: RepoLibBuilder<T>.() -> Unit): RepoLibRx<T> {
    val builder = RepoLibBuilder<T>()
    init(builder)
    return builder.build()
}


