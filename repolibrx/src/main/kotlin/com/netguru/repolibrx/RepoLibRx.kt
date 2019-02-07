package com.netguru.repolibrx

import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.datasource.DataSource
import com.netguru.repolibrx.strategy.DefaultRequestsStrategyFactory
import com.netguru.repolibrx.strategy.RequestsStrategyFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * [RepoLibRx] is an Main interface od the library [RepoLib]. The [RepoLib] is hidden
 * under this interface to make the library easy to replace or extend. The main purpose of
 * the interface is to hide main implementation and make the library component easy to replace in
 * the project.
 *
 * [<br/><br/>] **Important** [<br/>]
 * The library does not provide any kind of logic for managing object Ids or other data included
 * in data models. [RepoLibRx] is responsible for passing requests and its params to
 * the data sources in order defined by the strategy. Data wrapped in requests are passed without
 * any interference or modification.
 */
interface RepoLibRx<T> {

    /**
     * Output stream returned by [outputStream] function is responsible for transmitting data
     * from the data sources.
     * Data emission is triggered by the input events sent using one of the input methods:
     * [fetch] [create] [update] [delete]. Data is emitted using [BackpressureStrategy.LATEST]
     * [<br/><br/>]
     * Source for the data emission is selected by the [RequestsStrategyFactory]. The factory
     * should be implemented accroding to your needs or you can use [DefaultRequestsStrategyFactory]
     *
     */
    fun outputDataStream(): Flowable<T>

    /**
     * [fetch] method is responsible for triggering data emission from DataSources.
     * The request sent using this method will be passed to specific DataSource according
     * to the [RequestsStrategyFactory]. To trigger emission pass the [Query] object.
     * Each DataSource implementation should be able to resolve the Query.
     */
    fun fetch(query: Query): Completable

    /**
     * [create] method is used to pass object that should be created in DataSource.
     * Specific creation logic should be implemented under [DataSource] interface,
     * accordingly to selected storage implementation
     */
    fun create(entity: T): Completable

    /**
     * [update] method is used to pass object that should be updated in DataSource.
     * Specific logic for Querying by id and update data entity should be implemented under
     * [DataSource] interface, accordingly to selected storage implementation.
     */
    fun update(entity: T): Completable

    /**
     * [delete] method is used to pass object that should be removed in DataSource.
     * Specific logic for remove action and resolving Query object should be implemented under
     * [DataSource] interface, accordingly to selected storage implementation.
     */
    fun delete(query: Query): Completable
}