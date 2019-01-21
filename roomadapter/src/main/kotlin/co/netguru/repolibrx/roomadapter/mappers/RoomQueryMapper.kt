package co.netguru.repolibrx.roomadapter.mappers

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams

interface RoomQueryMapper {
    fun transformQueryByIdToStringPredicate(query: QueryById): String
    fun transformQueryAllToStringPredicate(query: QueryAll): String
    fun transformQueryToStringPredicate(query: Query): String
    fun transformQueryWithParamsToStringPredicate(query: QueryWithParams): String
}