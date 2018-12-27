package co.netguru.repolib.feature.demo.datasource.localstore

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DataDao : RealmObject() {
    @PrimaryKey
    var id: Long? = null
    var value: String? = null
}