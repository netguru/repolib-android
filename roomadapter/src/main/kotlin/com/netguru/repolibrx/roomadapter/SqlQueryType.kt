package com.netguru.repolibrx.roomadapter

import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

internal enum class SqlQueryType {
    SELECT,
    DELETE
}

/**
 * Helper function that is used to build SQL query string
 *
 * @param [operation] SQL operation: [SqlQueryType.SELECT] or [SqlQueryType.DELETE]
 * @param [what] identifier used to define what should be selected, usually `*`
 * @param [from] defines name of the table
 * @param [wherePredicates] it is a [String] that contains predicates used to find data matched some
 * conditions, e.g. id=1 value like "example"
 * [<br/>]
 * The functions build SQL query for SQLite database used by [Room] so, the param should contain
 * predicates based on SQLite queries
 *
 * @return [String] that contains SQL query. e.g [<br/>] *SELECT * FROM offers WHERE id=idParam*
 */
internal fun query(
        operation: SqlQueryType,
        what: String? = null,
        from: String,
        wherePredicates: String? = null
) = "$operation ${if (what.isNullOrBlank()) "" else "$what "}FROM $from ${if (wherePredicates.isNullOrBlank()) "" else "WHERE $wherePredicates"}"


/**
 * Helper function that is used to build SELECT SQL query. [select] function is using [query] function
 * and wraps the results into [SimpleSQLiteQuery] that is implementation of [SupportSQLiteQuery]
 * required by the [BaseDao]
 *
 * @param [what] identifier used to define what should be selected, usually `*`
 * @param [from] defines name of the table
 * @param [wherePredicates] it is a [String] that contains predicates used to find data matched some
 * conditions, e.g. id=1 value like "example"
 * [<br/>]
 * The functions build SQL query for SQLite database used by [Room] so, the param should contain
 * predicates based on SQLite queries
 *
 * @return [SupportSQLiteQuery] that contains SQL query. e.g [<br/>] *SELECT * FROM offers WHERE id=idParam*
 */
internal fun select(
        what: String,
        from: String,
        wherePredicates: String? = null
): SupportSQLiteQuery = SimpleSQLiteQuery(query(SqlQueryType.SELECT, what, from, wherePredicates))

/**
 * Helper function that is used to build DELETE SQL query. [delete] function is using [query] function
 * and wraps the results into [SimpleSQLiteQuery] that is implementation of [SupportSQLiteQuery]
 * required by the [BaseDao]
 *
 * @param [from] defines name of the table
 * @param [wherePredicates] it is a [String] that contains predicates used to find data matched some
 * conditions, e.g. id=1 value like "example"
 * [<br/>]
 * The functions build SQL query for SQLite database used by [Room] so, the param should contain
 * predicates based on SQLite queries
 *
 * @return [SupportSQLiteQuery] that contains SQL query. e.g [<br/>] *SELECT * FROM offers WHERE id=idParam*
 */
internal fun delete(
        from: String,
        wherePredicates: String? = null
): SupportSQLiteQuery = SimpleSQLiteQuery(query(SqlQueryType.DELETE, null, from, wherePredicates))