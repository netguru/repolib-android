package co.netguru.repolibrx.realmadapter

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery

interface RealmQueryMapper<D : RealmModel> {
    val daoClass: Class<D>

    fun transform(query: QueryById, realm: Realm): RealmQuery<D>
    fun transform(query: QueryWithParams, realm: Realm): RealmQuery<D>
    //todo describe why it is default
    fun transform(query: QueryAll, realm: Realm): RealmQuery<D> = realm.where(daoClass)

    fun transform(query: Query, realm: Realm): RealmQuery<D> = realm.where(daoClass)
}