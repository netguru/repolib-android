package com.netguru.repolibrx.roomadapter

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.netguru.repolibrx.RepoLibRx
import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper
import io.reactivex.Single

/**
 * Base implementation of Dao interface that is used by [Room] to execute operations on the database.
 * To use [RxRoomDataSource] this interface should by extended by the specific interface that
 * will be initialized by the [Room] according to its documentation
 * [https://developer.android.com/reference/android/arch/persistence/room/Dao]
 *
 * [<br/><br/>]
 * Instead of using annotations to define SQL queries, [BaseDao] functions enforces usage of
 * [SupportSQLiteQuery] by @[RawQuery] annotation. [SupportSQLiteQuery] allows to easily translate
 * [Query] objects into custom SQL queries.
 *
 * [<br/><br/>]
 * @param [D] type of data object that will be used to store or retrieve data to/from [Room] database.
 */
interface BaseDao<D> {

    /**
     * [query] function is responsible for fetching object from [Room] database using given [rawQuery].
     *
     * [<br/><br/>]
     * @param [rawQuery] is an object of type [SupportSQLiteQuery] that contains SQL query that was
     * built in [RxRoomDataSource] from [Query] object using [RoomQueryMapper]
     *
     * @return [Single] that contains [List] of objects of type [D] that matches to the given [rawQuery]
     */
    @RawQuery
    fun query(rawQuery: SupportSQLiteQuery): Single<List<D>>

    /**
     * [create] function is responsible for inserting [dataModel] to the [Room] database
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param [dataModel] of type [D] that should be created in the database. Id will be created
     * according to the current [Room] or data object setup, check
     * [https://developer.android.com/reference/android/arch/persistence/room/PrimaryKey.html#autoGenerate()]]
     */
    @Insert
    fun create(dataModel: D)

    /**
     * [update] function is responsible for updating [dataModel] to the [Room] database
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param [dataModel] data model that should be update in the database. The model will be find
     * using [PrimaryKey] field.
     */
    @Update
    fun update(dataModel: D)

    /**
     * [delete] function will remove object in [Room] database which match the given [rawQuery]
     *
     * @param [rawQuery] is an object of type [SupportSQLiteQuery] that contains SQL query that was
     * built in [RxRoomDataSource] from [Query] object using [RoomQueryMapper]
     *
     * @return number of deleted items. This return type is enforced by the [Room] specification
     * but the result will be dropped by the [RepoLibRx]
     */
    @RawQuery
    fun delete(rawQuery: SupportSQLiteQuery): Int
}