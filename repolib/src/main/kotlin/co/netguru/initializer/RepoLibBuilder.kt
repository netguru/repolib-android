package co.netguru.initializer

import co.netguru.RepoLib
import co.netguru.datasource.DataSource
import co.netguru.datasource.QueueDataSourceController
import co.netguru.queue.DefaultQueue
import co.netguru.queue.RequestQueue
import co.netguru.strategy.DefaultSourcingStrategy
import co.netguru.strategy.SourcingStrategy
import kotlin.properties.Delegates

//todo this builder will be used to initialize repository with defaults,
//todo defaults will be implemented in RPI-33
class RepoLibBuilder<T> {

    var localDataSourceController: DataSource<T> by Delegates.notNull()

    var remoteDataSourceController: DataSource<T> by Delegates.notNull()

    var sourcingStrategy: SourcingStrategy = DefaultSourcingStrategy()

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

open class RemoteDataSourceControllerBuilder<T> {

    var dataSource: DataSource<T> by Delegates.notNull()

    private var requestQueue: RequestQueue<T> = DefaultQueue()

    fun build(): DataSource<T> {
        return QueueDataSourceController(dataSource, requestQueue)
    }
}

fun <T> createRemoteController(init: RemoteDataSourceControllerBuilder<T>.() -> Unit): DataSource<T> {
    val builder = RemoteDataSourceControllerBuilder<T>()
    init(builder)
    return builder.build()
}



