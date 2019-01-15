package co.netguru.repolibrx.roomadapter

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

internal enum class SqlQueryType {
    SELECT,
    DELETE
}

internal fun query(
        operation: SqlQueryType,
        what: String? = null,
        from: String,
        wherePredicates: String? = null
) = "$operation ${if (what.isNullOrBlank()) "" else "$what "}FROM $from ${if (wherePredicates.isNullOrBlank()) "" else "WHERE $wherePredicates"}"


internal fun select(
        what: String,
        from: String,
        wherePredicates: String? = null
): SupportSQLiteQuery = SimpleSQLiteQuery(query(SqlQueryType.SELECT, what, from, wherePredicates))

internal fun delete(
        from: String,
        wherePredicates: String? = null
): SupportSQLiteQuery = SimpleSQLiteQuery(query(SqlQueryType.DELETE, null, from, wherePredicates))