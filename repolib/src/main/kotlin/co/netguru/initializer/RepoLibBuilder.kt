package co.netguru.initializer

import co.netguru.RepoLib
import co.netguru.datasource.DataSource
import co.netguru.strategy.DefaultRequestsStrategy
import co.netguru.strategy.RequestsStrategy
import kotlin.properties.Delegates

class RepoLibBuilder<T> {

    var localDataSourceController: DataSource<T> by Delegates.notNull()

    var remoteDataSourceController: DataSource<T> by Delegates.notNull()

    private var requestsStrategy: RequestsStrategy = DefaultRequestsStrategy()

    fun build(): RepoLib<T> {
        return RepoLib(
                localDataSource = localDataSourceController,
                remoteDataSource = remoteDataSourceController,
                requestsStrategy = requestsStrategy
        )
    }
}

fun <T> createRepo(init: RepoLibBuilder<T>.() -> Unit): RepoLib<T> {
    val builder = RepoLibBuilder<T>()
    init(builder)
    return builder.build()
}


