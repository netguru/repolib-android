package co.netguru.repolibrx.roomdatastorce

import androidx.room.Room
import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.roomadapter.RxRoomDataSource
import co.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper

/**
 * Example implementation of the [RoomQueryMapper] that is used by the
 * [RxRoomDataSource] to translate [RepoLib] [Query] objects to SQL queries required by the [Room] storage.
 */
class NotesQueryMapper : RoomQueryMapper {
    override fun transformQueryByIdToStringPredicate(query: QueryById) = "id=${query.identifier}"
    override fun transformQueryAllToStringPredicate(query: QueryAll) = ""
    override fun transformQueryToStringPredicate(query: Query) = ""
    override fun transformQueryWithParamsToStringPredicate(query: QueryWithParams) = "id=${query.param<Long>("id")}"
}