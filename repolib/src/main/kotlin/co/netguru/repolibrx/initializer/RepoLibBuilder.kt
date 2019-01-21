package co.netguru.repolibrx.initializer

import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.DefaultRequestsStrategyFactory
import co.netguru.repolibrx.strategy.RequestsStrategyFactory
import kotlin.properties.Delegates

/**
 * Builder class can be used for easier initialization of the [RepoLib]. The builder is used by
 * the higher-order function [createRepo] to make building process as easiest as possible.
 */
class RepoLibBuilder<T> {

    /**
     * Properties mentioned below are used to initialize [RepoLib]. All of them are public to allow
     * setup all the dependencies using [createRepo] method and [RepoLibBuilder] lambda.
     */
    var localDataSource: DataSource<T> by Delegates.notNull()
    var remoteDataSource: DataSource<T> by Delegates.notNull()
    var requestsStrategyFactory: RequestsStrategyFactory = DefaultRequestsStrategyFactory()

    fun build(): RepoLibRx<T> {
        return RepoLib(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                requestsStrategyFactory = requestsStrategyFactory
        )
    }
}

/**„
 * [createRepo] is an Higher-order function that takes [init] lambda and applies it on builder class
 * to build the [RepoLibRx] controller. It can be easily used by the Dependency Injection framework like Dagger, etc.
 *
 * @param [init] lambda that is responsible for assigning fields in [RepoLibRx] that are required by
 * the [RepoLibRx] class constructor.
 *
 * @return [RepoLibRx] it returns properly initialized controller of the RepoLib library
 */
fun <T> createRepo(init: RepoLibBuilder<T>.() -> Unit): RepoLibRx<T> {
    val builder = RepoLibBuilder<T>()
    init(builder)
    return builder.build()
}


