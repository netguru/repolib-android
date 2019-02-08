package com.netguru.repolibrx.sample.feature.demo.datasource.localstore

import com.netguru.repolibrx.RepoLib.Companion.UNDEFINED
import com.netguru.repolibrx.realmadapter.RealmDataMapper
import com.netguru.repolibrx.realmadapter.RxRealmDataSource
import com.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.netguru.repolibrx.sample.feature.demo.data.SourceType
import io.realm.RealmObject

/**
 * [DataMapper] is an example implementation of the [RealmDataMapper] that is used by
 * [RxRealmDataSource] to translate data entity model to the [RealmObject]. In this case
 * from [DemoDataEntity] model to [NoteLocalRealmObject] model and reverse.
 *
 * This class also contains example of simple logic responsible for managing the data ids.
 */
class DataMapper : RealmDataMapper<DemoDataEntity, NoteLocalRealmObject> {
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