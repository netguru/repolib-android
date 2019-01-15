package co.netguru.repolibrx.sample.feature.demo.datasource.localstore

import co.netguru.repolibrx.RepoLib.Companion.UNDEFINED
import io.realm.RealmObject

open class NoteLocalRealmObject : RealmObject() {
    var id: Long = UNDEFINED
    var value: String? = null
}