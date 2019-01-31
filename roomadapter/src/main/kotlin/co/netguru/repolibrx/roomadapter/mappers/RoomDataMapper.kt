package co.netguru.repolibrx.roomadapter.mappers

import androidx.room.Room
import co.netguru.repolibrx.roomadapter.BaseDao
import co.netguru.repolibrx.roomadapter.RxRoomDataSource
import co.netguru.repolibrx.RepoLib

/**
 * [RoomDataMapper] is an interface that represents data transformation util. It's responsibility is
 * to transform data from data/business layer model [E] to local data model [D] required by
 * the [Room].
 *
 * @param [E] type of the business/data layer data model. It is equal to the type used by
 * the [RxRoomDataSource] and [RepoLib]
 * @param [D] type of the local data model used by the [Room] to store data. It shuld be equal
 * to the type used by the [BaseDao]
 */
interface RoomDataMapper<E, D> {

    /**
     * Higher order function [transformEntityToDaoModel] should contain logic for transforming data
     * from the data model of type [E] to [D].
     *
     * @return lambda that contains transformation logic
     */
    fun transformEntityToDaoModel(): (E) -> D

    /**
     * Higher order function [transformEntityToDaoModel] should contain logic for transforming data
     * from the mode of type [E] to [D].
     *
     * @return lambda that contains transformation logic
     */
    fun transformModelToEntity(): (D) -> E
}