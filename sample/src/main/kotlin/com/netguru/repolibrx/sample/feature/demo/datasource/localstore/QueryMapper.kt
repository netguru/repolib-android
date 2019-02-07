package com.netguru.repolibrx.sample.feature.demo.datasource.localstore

import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.QueryById
import com.netguru.repolibrx.data.QueryWithParams
import com.netguru.repolibrx.realmadapter.RealmQueryMapper
import com.netguru.repolibrx.roomadapter.RxRoomDataSource
import io.realm.Realm
import io.realm.RealmQuery

/**
 * Example implementation of the [RealmQueryMapper] that is used by the
 * [RxRoomDataSource] to translate [RepoLib] [Query] objects to SQL queries required by the [Realm] storage.
 */
class QueryMapper(override val daoClass: Class<NoteLocalRealmObject> = NoteLocalRealmObject::class.java)
    : RealmQueryMapper<NoteLocalRealmObject> {

    override fun transform(query: QueryById, realm: Realm): RealmQuery<NoteLocalRealmObject> = realm
            .where(daoClass).equalTo("id", query.identifier)

    override fun transform(query: QueryWithParams, realm: Realm): RealmQuery<NoteLocalRealmObject> {
        val realmQuery = realm.where(daoClass)
        val idKey = "id"
        val valueKey = "value"
        if (query.params.containsKey(idKey)) realmQuery.equalTo(idKey, query.param<Long>(idKey))
        if (query.params.containsKey(valueKey)) realmQuery.equalTo(valueKey, query.param<String>(valueKey))
        return realmQuery
    }
}