package co.netguru.repolibrx.sample.feature.demo.datasource.localstore

import co.netguru.repolibrx.RepoLib.Companion.UNDEFINED
import co.netguru.repolibrx.realmadapter.RealmDataMapper
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.data.SourceType


class DataMapper : RealmDataMapper<DemoDataEntity, NoteLocalRealmObject> {
    //    todo implement common solution
    private var highestId = UNDEFINED

    override fun transformToEntity(): (NoteLocalRealmObject) -> DemoDataEntity = { dao ->
        DemoDataEntity(saveHighestId(dao.id), dao.value!!, SourceType.LOCAL)
    }

    override fun rewriteValuesToDao(
            entity: DemoDataEntity,
            emptyDaoObject: NoteLocalRealmObject
    ): DemoDataEntity {
        emptyDaoObject.id = if (entity.id != UNDEFINED) entity.id else highestId + 1
        emptyDaoObject.value = entity.value
        return entity.copy(id = emptyDaoObject.id, sourceType = SourceType.LOCAL)
    }

    private fun saveHighestId(currentDaoId: Long): Long {
        highestId = if (currentDaoId > highestId) currentDaoId else highestId
        return currentDaoId
    }
}