package com.netguru.repolibrx.sample.feature.demo.datasource.roomdatastorce

import androidx.room.Room
import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.QueryAll
import com.netguru.repolibrx.data.QueryById
import com.netguru.repolibrx.data.QueryWithParams
import com.netguru.repolibrx.roomadapter.RxRoomDataSource
import com.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper

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