package com.netguru.repolibrx.sample.feature.demo.datasource.localstore

import com.netguru.repolibrx.RepoLib.Companion.UNDEFINED
import io.realm.RealmObject

open class NoteLocalRealmObject : RealmObject() {
    var id: Long = UNDEFINED
    var value: String? = null
}