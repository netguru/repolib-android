package co.netguru.repolib.feature.demo.datasource.localstore

import io.realm.RealmObject

open class DataDao : RealmObject() {
    var id: Long? = null
    var value: String? = null
}