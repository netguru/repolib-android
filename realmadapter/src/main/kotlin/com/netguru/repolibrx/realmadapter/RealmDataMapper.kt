package com.netguru.repolibrx.realmadapter

import com.netguru.repolibrx.RepoLib
import io.realm.Realm
import io.realm.RealmObject

/**
 * [RealmDataMapper] interface is an abstraction that represents data transformation logic
 * of specific data models.
 *
 * @param [E] represents type of data entity used by [RxRealmDataSource] and [RepoLib]
 * @param [D] represents data type that extends [RealmObject], this model is used by the [Realm]
 * database to store data.
 */
interface RealmDataMapper<E, D : RealmObject> {
    /**
     * [transformToEntity] method should contains logic that creates new object of type [E]
     * initialized with data stored in [RealmObject] [D]. This function is used by the [RxRealmDataSource]
     * to transform models fetched from [Realm] database to the data entities that are understood by the
     * [RepoLib].
     *
     * [<br/><br/>]
     * [D] existing [RealmObject] collected from [Realm] database
     * @return [E] new object that contains data from [D]
     */
    fun transformToEntity(): (D) -> E

    /**
     * [rewriteValuesToDao] function function is responsible for moving data from existing [entity] model of type
     * [E] to *existing* [RealmObject] [emptyDaoObject] of type D. If model of type [E] contains some data
     * the library will overwrite it.
     * [<br/><br/>]
     * [rewriteValuesToDao] is used in reverse to [transformToEntity] but is using already created
     * [RealmObject] object. The reason of such approach is that [Realm] requires to create object
     * using [Realm.createObject] function instead of manual creation using constructors. Usage
     * of [Realm.createObject] allows to create object associated with the database. For more
     * information check [https://realm.io/docs/java/latest/api/io/realm/Realm.html]
     *
     * [<br/><br/>]
     * @param entity is a data model of type [E] used by [RxRealmDataSource] and [RepoLib]. It
     * contains data that should be moved to the [emptyDaoObject]
     * @param emptyDaoObject is [RealmObject] already created by the [RxRealmDataSource] using
     * [Realm.createObject] function
     */
    fun rewriteValuesToDao(entity: E, emptyDaoObject: D): E
}