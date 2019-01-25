package co.netguru.repolibrx.realmadapter

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * [RealmQueryMapper] interface is an abstraction that represents transformation logic
 * from [Query] to specific cases of [RealmQuery] objects.
 *
 * [<br/><br/>]
 * [D] is type of custom [RealmObject] used by [Realm] database to store the custom data.
 */
interface RealmQueryMapper<D : RealmObject> {

    /**
     * An abstract field that holds [Class] of type [D]. [Class] is required by the [Realm] API
     * to find or create object of specific types e.g [Realm.createObject]
     */
    val daoClass: Class<D>

    /**
     * [transform] method should contain logic for transforming [QueryById] passed as [query]
     * param to the [RealmQuery] using [Realm] instance. Transformation of [QueryById] is required
     * by [RxRealmDataSource.update] to properly find element to update in [Realm] database
     * using id constraint.
     * [<br/><br/>]
     * Example
     * [<br/>]
     * realm.where(daoClass).equalTo("id", query.identifier)
     *
     * [<br/><br/>]
     * @param query of [QueryById] contains id of requested element.
     * @param realm that contains [Realm] instance that is needed to create [RealmQuery]
     * @return [RealmQuery] of type [daoClass] with id == [QueryById.identifier]
     */
    fun transform(query: QueryById, realm: Realm): RealmQuery<D>

    /**
     * [transform] method should contain logic for transforming [QueryWithParams] passed as [query]
     * param to the [RealmQuery] using [Realm] instance. Transformation of [QueryWithParams] to
     * properly fetch elements from [Realm] database using id constraint.
     * [<br/><br/>]
     * Example
     * [<br/>]
     * realm.where(daoClass).equalTo("value", query.param["value"])
     *
     * [<br/><br/>]
     * @param query of [QueryWithParams] contains map of params defining requested element.
     * @param realm that contains [Realm] instance that is needed to create [RealmQuery]
     * @return [RealmQuery] of type [daoClass] with all params from [query].
     */
    fun transform(query: QueryWithParams, realm: Realm): RealmQuery<D>

    /**
     * [transform] method should contain logic for transforming [QueryAll] passed as [query]
     * param to the [RealmQuery] using [Realm] instance. Transformation of [QueryAll] is **not required**
     * to be implemented. Default implementation of query all can be used.
     *
     * [<br/><br/>]
     * @param query of [QueryAll] is an object that should be translated into [RealmQuery] for
     * all elements of requested type.
     * @param realm that contains [Realm] instance that is needed to create [RealmQuery]
     * @return [RealmQuery] of type [daoClass] that pointing all elements.
     */
    fun transform(query: QueryAll, realm: Realm): RealmQuery<D> = realm.where(daoClass)

    /**
     * [transform] method should contain logic for transforming any custom [Query] to the [RealmQuery]
     * using [Realm] instance. Transformation of [Query] is **not required**
     * to be implemented. Default implementation can be used.
     *
     * [<br/><br/>]
     * @param query of type [Query] is an object that can be used to resolve any custom query objects.
     * @param realm that contains [Realm] instance that is needed to create [RealmQuery]
     * @return [RealmQuery] of type [daoClass] that is translated version of passed custom [query]
     */
    fun transform(query: Query, realm: Realm): RealmQuery<D> = realm.where(daoClass)
}