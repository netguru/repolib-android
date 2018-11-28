package co.netguru.initializer

import co.netguru.RepoLib
import co.netguru.cache.RequestQueue
import co.netguru.datasource.DataSource
import co.netguru.datasource.DataSourceController
import co.netguru.datasource.LocalDataSourceController
import co.netguru.datasource.RemoteDataSourceController
import co.netguru.strategy.SourcingStrategy
import kotlin.properties.Delegates

//todo this builder will be used to initialize repository with defaults,
//todo defaults will be implemented in RPI-33
class RepoLibBuilder<T> {

    var localDataSourceController: DataSourceController<T> by Delegates.notNull()

    var remoteDataSourceController: DataSourceController<T> by Delegates.notNull()

    var sourcingStrategy: SourcingStrategy by Delegates.notNull()

    fun build(): RepoLib<T> {
        return RepoLib(
                localDataSource = localDataSourceController,
                remoteDataSource = remoteDataSourceController,
                sourcingStrategy = sourcingStrategy
        )
    }
}

fun <T> createRepo(init: RepoLibBuilder<T>.() -> Unit): RepoLib<T> {
    val builder = RepoLibBuilder<T>()
    init(builder)
    return builder.build()
}

open class LocalDataSourceControllerBuilder<T> {
    var dataSource: DataSource<T> by Delegates.notNull()

    open fun build(): DataSourceController<T> {
        return LocalDataSourceController(dataSource)
    }
}

open class RemoteDataSourceControllerBuilder<T> : LocalDataSourceControllerBuilder<T>() {
    var requestQueue: RequestQueue<T> by Delegates.notNull()

    override fun build(): DataSourceController<T> {
        return RemoteDataSourceController(dataSource, requestQueue)
    }
}

fun <T> createLocalController(init: LocalDataSourceControllerBuilder<T>.() -> Unit): DataSourceController<T> {
    val builder = LocalDataSourceControllerBuilder<T>()
    init(builder)
    return builder.build()
}

fun <T> createRemoteControllerWithDefaultQueueStrategy(init: RemoteDataSourceControllerBuilder<T>.() -> Unit): DataSourceController<T> {
    val builder = RemoteDataSourceControllerBuilder<T>()
    init(builder)
    return builder.build()
}



