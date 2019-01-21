package co.netguru.repolibrx.sample.feature.demo.datasource.localstore

import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.realmadapter.RealmQueryMapper
import io.realm.Realm
import io.realm.RealmQuery

class QueryMapper(override val daoClass: Class<NoteLocalRealmObject> = NoteLocalRealmObject::class.java) : RealmQueryMapper<NoteLocalRealmObject> {

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