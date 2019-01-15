package co.netguru.repolibrx.realmadapter

import io.realm.RealmObject

interface RealmDataMapper<ENTITY, DAO : RealmObject> {
    fun transformToEntity(): (DAO) -> ENTITY
    fun rewriteValuesToDao(entity: ENTITY, emptyDaoObject: DAO): ENTITY
}