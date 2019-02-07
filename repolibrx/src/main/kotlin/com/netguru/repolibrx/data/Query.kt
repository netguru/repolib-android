package com.netguru.repolibrx.data

import com.netguru.repolibrx.datasource.DataSource

/**
 * Query interface is used to encapsulate parameters used by [DataSource] implementation.
 *
 * [<br/><br/>]
 * **Translation to querying logic specific to the [DataSource] implementation should be
 * manually implemented in DataSource.**
 */
interface Query

/**
 * QueryAll is example of specific type of the [Query]. It is an abstraction that represents
 * querying of all items of specific type available in the DataSource.
 *
 * [<br/><br/>]
 * **Translation to querying logic specific to the [DataSource] implementation should be
 * manually implemented in DataSource.**
 */
object QueryAll : Query

/**
 * QueryById is an specific type of the [Query] that represent querying elements by id. This kind of
 * Query should be used in [DataSource.update] method implementation to find element that should be updated.
 * Implementation of translation for specific querying logic is obligatory to make [DataSource.update]
 * method working correctly.
 *
 * [<br/><br/>]
 * **Translation to querying logic specific to the [DataSource] implementation should be
 * manually implemented in DataSource.**
 */
data class QueryById(val identifier: Long) : Query

/**
 * QueryWithParams is an specific type of [Query] that is designed to pass other parameters for
 * querying logic. Parameters can by passed as pairs *param name : expected value*. Set of pairs
 * is held as [MutableMap]<String, Any>. The map is accessible directly as public property named [params].
 * Param can be retrieved by accessing the map through the property or using [param] function.
 *
 * [<br/><br/>]
 * **Translation to querying logic specific to the [DataSource] implementation should be
 * manually implemented in DataSource.**
 *
 * [<br/><br/>]
 * @param [paramPairs] is set of [Pair] that will be used to initialize [params] map.
 */
class QueryWithParams(vararg paramPairs: Pair<String, Any>) : Query {

    /**
     * [params] [MutableMap] contains parameters pairs where the Key is holding name of
     * the parameter and The Value is holding object representing parameter constraint. The params
     * should be manually resolved in [DataSource] implementation to fulfill its specific query
     * requirements, e.g. Realm have different logic for handling Queries by params than Room or Retrofit.
     */
    val params = mutableMapOf<String, Any>()

    init {
        paramPairs.forEach { (key, value) -> params[key] = value }
    }

    /**
     * [param] it is an inline function that allows to access query params by key. The key is a
     * parameter name. Function is performing automatic casting by using type parameter [T].
     * If requested type is different type than requested by [T] the function throws an exception.
     * [<br/><br/>]
     * @param [key] name of the parameter
     * @param [T] expected type of the parameter
     * [<br/><br/>]
     * @return parameter retrieved from [params] map casted to type [T]
     */
    inline fun <reified T> param(key: String): T = params[key] as T
}