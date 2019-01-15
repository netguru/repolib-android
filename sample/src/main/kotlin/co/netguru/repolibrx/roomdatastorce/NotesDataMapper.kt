package co.netguru.repolibrx.roomdatastorce

import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.roomadapter.mappers.RoomDataMapper
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.data.SourceType

class NotesDataMapper : RoomDataMapper<DemoDataEntity, Note> {

    override fun transformEntityToDaoModel(): (DemoDataEntity) -> Note = { entity ->
        if (entity.id != RepoLib.UNDEFINED) {
            Note(entity.id.toInt(), entity.value)
        } else {
            Note(value = entity.value)
        }
    }

    override fun transformModelToEntity(): (Note) -> DemoDataEntity = {
        DemoDataEntity(it.id.toLong(), it.value, SourceType.LOCAL)
    }
}