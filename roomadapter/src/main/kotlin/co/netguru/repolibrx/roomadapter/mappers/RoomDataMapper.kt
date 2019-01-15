package co.netguru.repolibrx.roomadapter.mappers

interface RoomDataMapper<ENTITY, DAO_MODEL> {
    fun transformEntityToDaoModel(): (ENTITY) -> DAO_MODEL
    fun transformModelToEntity(): (DAO_MODEL) -> ENTITY
}