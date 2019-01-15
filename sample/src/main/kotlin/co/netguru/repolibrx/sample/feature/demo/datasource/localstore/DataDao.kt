package co.netguru.repolibrx.sample.feature.demo.datasource.localstore

import co.netguru.repolibrx.realmadapter.RxRealmDataSource.Companion.UNDEFINED
import io.realm.RealmObject

open class DataDao : RealmObject() {
    var id: Long = UNDEFINED
    var value: String? = null
}