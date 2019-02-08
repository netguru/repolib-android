package com.netguru.repolibrx.roomadapter

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteQuery
import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.RepoLibRx
import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.QueryAll
import com.netguru.repolibrx.data.QueryById
import com.netguru.repolibrx.data.QueryWithParams
import com.netguru.repolibrx.datasource.DataSource
import com.netguru.repolibrx.roomadapter.mappers.RoomDataMapper
import com.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * [RxRoomDataSource] is an implementation of the [DataSource] interface for [RepoLib] based on
 * [Room] storage. The main goal of this adapter is to make implementation of the data layer
 * for the applications using [RepoLib] as easy as possible. [RxRoomDataSource] contains
 * logic responsible for all operations required by [DataSource] interface like [create], [update],
 * [delete] and [fetch].
 *
 * [<br/><br/>]
 * To fully implement data operations, it is required to implement two mappers to
 * show the [RxRoomDataSource] how to map data entities and how to resolve [Query] objects.
 *
 * [<br/><br/>]
 * @param [E] represents type of the data input/output entity. The type should be equal to the input
 * type param in [RepoLib]
 * @param [D] represents data model that will be used by the [Room] to store the data.
 * @param tableName is a name SQL table used to store object of type [D]. Name will be used to
 * build SQL queries
 * [<br/>]
 * e.g
 * *SELECT * FROM [tableName]*
 *
 * @param roomDataMapper is an object that represents [RoomDataMapper] implementation.
 * The [RoomDataMapper] is responsible transforming data from entity data model [E] to [D].
 * @param queryMapper is an object that represents [RoomQueryMapper] implementation.
 * The [RoomDataMapper] is used to transform [Query] objects to Room specific queries based on SQL.
 */
open class RxRoomDataSource<E, D>(
        private val tableName: String,
        private val baseDao: BaseDao<D>,
        private val queryMapper: RoomQueryMapper,
        private val roomDataMapper: RoomDataMapper<E, D>
) : DataSource<E> {

    /**
     * [create] method is responsible for creation of the entities passed as [entity] param.
     * Method will trigger creation of the object in [Room] database using [RoomQueryMapper]
     * to transform [entity] to [D] and [baseDao]] to execute the operation on database.
     * All async operation can be achieved using RxJava/RxKotlin tools and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param entity of type [E] that contains object that should be created in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that will emit created object and complete event when
     * operation is done.
     */
    override fun create(entity: E): Observable<E> = Observable
            .fromCallable { entity }
            .map(roomDataMapper.transformEntityToDaoModel())
            .flatMap { item ->
                Completable.fromAction { baseDao.create(item) }
                        .andThen(Observable.just(item))
                        .map(roomDataMapper.transformModelToEntity())
            }

    /**
     * [update] method is responsible for updating of the entities passed as [entity] param.
     * Method will trigger updating of the object in [Room] database using [RoomQueryMapper]
     * to transform [entity] to [D], and [baseDao]] to execute the operation on database.
     * All async operation can be achieved using RxJava/RxKotlin tools and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param entity of type [E] that contains object that should be updated in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that will emit updated object and complete event when
     * operation is done.
     */
    override fun update(entity: E): Observable<E> = Observable
            .fromCallable { entity }
            .map(roomDataMapper.transformEntityToDaoModel())
            .flatMapCompletable { item ->
                Completable.fromAction { baseDao.update(item) }
            }.andThen(Observable.just(entity))

    /**
     * [delete] method is responsible for deleting of the object that can be found using constraints
     * defined by [query] param. Method will trigger deleting of the object in [Room] database using
     * [RoomQueryMapper] to transform [Query] object to [SupportSQLiteQuery], and [baseDao] to execute
     * the operation on database. All async operation can be achieved using RxJava/RxKotlin tools and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param query that contains constraints that defines what object should be deleted in DataSource.
     * This query will be translated to [SupportSQLiteQuery] using [RoomQueryMapper]. [<br/><br/>]
     * @return [Observable] of type [E] that will emit complete event deleting is complete. Observable
     * **will not** emit any data entities.
     */
    override fun delete(query: Query): Observable<E> = Observable
            .fromCallable { query }
            .flatMapCompletable {
                Completable.fromAction {
                    baseDao.delete(delete(tableName, getQueryPredicates(query)))
                }
            }
            .toObservable()

    /**
     * [fetch] method is responsible for fetching all object that can be found using constraints
     * defined by [query] param. Method will trigger deleting of the object in [Room] database using
     * [RoomQueryMapper] to transform [Query] object to [SupportSQLiteQuery], and [baseDao] to execute
     * the operation on database. All async operation can be achieved using RxJava/RxKotlin tools and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param query that contains constraints that defines what object should be deleted in DataSource.
     * This query will be translated to [SupportSQLiteQuery] using [RoomQueryMapper]. [<br/><br/>]
     * @return [Observable] of type [E] that emits all objects that matching [query].
     */
    override fun fetch(query: Query): Observable<E> = Observable
            .fromCallable { query }
            .flatMapSingle {
                baseDao.query(select("*", tableName, getQueryPredicates(query)))
            }
            .flatMapIterable { it }
            .map(roomDataMapper.transformModelToEntity())

    private fun getQueryPredicates(query: Query): String = when (query) {
        is QueryById -> queryMapper.transformQueryByIdToStringPredicate(query)
        is QueryAll -> queryMapper.transformQueryAllToStringPredicate(query)
        is QueryWithParams -> queryMapper.transformQueryWithParamsToStringPredicate(query)
        else -> queryMapper.transformQueryToStringPredicate(query)
    }
}