package co.netguru.repolibrx.datasource

import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.data.Query
import io.reactivex.Observable

/**
 * [DataSource] provides unified interface for data sources. [RepoLibRx] is using this
 * interface to perform specific requests on data sources. This interface should be individually
 * implemented by the specific data sources like e.g. Realm, Room, Retrofit, etc.
 * The library provides additional adapters for most common data sources like Realm or Room.
 * [<br/><br/>]
 * For more information about specific adapters check [https://github.com/netguru/repolib-android]
 */
interface DataSource<E> {

    /**
     * Function that should implement logic for object creation in the data source.
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param entity of type [E] that contains new object that should be created in DataSource
     * [<br/><br/>]
     * @return [Observable] of type [E] that returns already created entity. Returning of the
     * new entity is optional, but it is recommended to at least publish Complete event,
     * to allow request strategies to be completed. Any kind of data published using this
     * [Observable] will be emitted using [RepoLibRx.outputDataStream]
     */
    fun create(entity: E): Observable<E>

    /**
     * Function that should implement logic for object updating in the data source.
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param entity of type [E] that contains object that should be updated in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that holding events with already updated entity. Returning of the
     * updated entity is optional, but it is recommended to at least publish Complete event,
     * to allow request strategies to be completed. Any kind of data published using this
     * [Observable] will be emitted using [RepoLibRx.outputDataStream]
     */
    fun update(entity: E): Observable<E>

    /**
     * Function that should implement logic for deleting data objects in the data source.
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param query that contains constraints that defines what object should be deleted in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that holding events with already updated entity. Returning of the
     * updated entity is optional, but it is recommended to at least publish Complete event,
     * to allow request strategies to be completed. Any kind of data published using this
     * [Observable] will be emitted using [RepoLibRx.outputDataStream]
     */
    fun delete(query: Query): Observable<E>

    /**
     * Function that should implement logic for deleting data objects in the data source.
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param query that contains constraints that defines what object should be deleted in DataSource.
     * All constraints (params and its values) included under [Query] abstraction should be resolved
     * manually for each [DataSource] implementation.
     * [<br/><br/>]
     * @return [Observable] of type [E] that holding events with already deleted entity. Returning of the
     * updated entity is optional, but it is recommended to at least publish Complete event,
     * to allow request strategies to be completed. Any kind of data published using this
     * [Observable] will be emitted using [RepoLibRx.outputDataStream]
     */
    fun fetch(query: Query): Observable<E>
}