package co.netguru.repolibrx.roomadapter.mappers

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.roomadapter.RxRoomDataSource
import androidx.room.Room


/**
 * [RoomQueryMapper] interface is an abstraction that represents transformation logic
 * from [Query] to specific SQL WHERE statements
 *
 * **All function of this interface should return String that contains criteria that specifies
 * row that should be selected. This criteria will be attached by [RxRoomDataSource] right after
 * *WHERE* statement in SQL query.
 *
 * [<br/><br/>]
 * e.g
 * for SQL Query: *SELECT * FROM offers WHERE id=idParam*
 * [<br/>] function should return [String] that contains "id=idParam"
 *
 */
interface RoomQueryMapper {

    /**
     * [transformQueryByIdToStringPredicate] should contain logic that transform [QueryById] to
     * String with criteria related with matching by id. Implementation of this method is
     * required by [RxRoomDataSource.update] function to proper handling of updating models in [Room].
     *
     * [<br/><br/>]
     * @param query [QueryById] contains value of object id that should be found.
     * @return [String] with criteria that will select rows with matching id, e.g. *"id=${query.identifier}"*
     */
    fun transformQueryByIdToStringPredicate(query: QueryById): String

    /**
     * [transformQueryWithParamsToStringPredicate] should contain logic that transform [QueryWithParams] to
     * String with criteria related with multiple custom parameters included in the [query].
     * Implementation of this method is required by [RxRoomDataSource.fetch] function to proper
     * fetching models from [Room].
     *
     * [<br/><br/>]
     * @param query [QueryWithParams] contains multiple constrains to defines requested values of the data params
     * @return [String] with criteria that will select rows with matching all values and params
     * included in [query] id, e.g. *"value like ${query.param<String>("value")} OR count=${query.param<Long>("count")}"*
     */
    fun transformQueryWithParamsToStringPredicate(query: QueryWithParams): String

    /**
     * [transformQueryAllToStringPredicate] should contain logic that transform [QueryAll] to
     * String with criteria that matching all elements. Implementation of this method is **not required**.
     * By default this method returns empty [String] that is equals to the SQL query without *WHERE*
     * statement like *SELECT * FROM offers*
     *
     * [<br/><br/>]
     * @param query [QueryAll] query of [QueryAll] is an object that should be translated into constraints
     * that pointing all elements.
     * @return [String] with criteria that will select all available rows in [Room] database by default
     * it returns empty [String]
     */
    fun transformQueryAllToStringPredicate(query: QueryAll): String = ""

    /**
     * [transformQueryToStringPredicate]  should contain logic for transforming any custom [Query] to
     * String constraints for SQL WHERE statement. Transformation of [Query] is **not required**
     * to be implemented. Default implementation can be used. By default this method returns empty
     * [String] that is equals to the SQL query without *WHERE* statement like *SELECT * FROM offers*
     *
     * [<br/><br/>]
     * @param query of type [Query] is an object that can be used to resolve any custom query objects.
     * @return [String] with criteria that will select all rows from [Room] database that matches
     * constraints defined by the custom [Query] object passed in [query]
     */
    fun transformQueryToStringPredicate(query: Query): String = ""
}