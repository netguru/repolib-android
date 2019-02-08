package com.netguru.repolibrx.realmadapter

import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.RepoLibRx
import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.QueryAll
import com.netguru.repolibrx.data.QueryById
import com.netguru.repolibrx.data.QueryWithParams
import com.netguru.repolibrx.datasource.DataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.*

/**
 * [RxRealmDataSource] is an implementation of the [DataSource] interface for [RepoLib] based on
 * [Realm] storage. The main goal of this adapter is to make implementation of the data layer
 * for the applications using [RepoLib] as easy as possible. [RxRealmDataSource] contains
 * logic responsible for all operations required by [DataSource] interface like [create], [update],
 * [delete] and [fetch].
 *
 * [<br/><br/>]
 * To fully implement data operations it is required to implement two mappers to
 * show the [RxRealmDataSource] how to map data entities and how to resolve [Query] objects.
 *
 * [<br/><br/>]
 * @typ [E] represents type of the data input/output entity. The type should be equal to the input
 * type param in [RepoLib]. Data entity [E] should extend [Identified] interface.
 * @param [D] represents data model that will be used by the [Realm] to store the data. [D] should
 * implement [RealmObject] abstract class in accordance to the Realm documentation:
 * [https://realm.io/docs/java/4.3.3/api/io/realm/RealmObject.html]
 *
 *[<br/><br/>]
 * @param realmConfiguration [RealmConfiguration] is an object that represents specific configuration
 * used by [Realm], it is required by [RxRealmDataSource] for managing [Realm] instances. Configuration
 * should be initiated before initialization of the [RepoLib] and [RxRealmDataSource].
 *
 * @param dataMapper is an object that represents [RealmDataMapper] implementation.
 * The [RealmDataMapper] is responsible for transforming data from entity data model [E] to [RealmObject]
 * represented by the generic type [E] and reverse.
 * @param queryMapper is an object that represents [RealmQueryMapper] implementation.
 * The [RealmDataMapper] is responsible for translating [Query] objects to Realm specific queries.
 */
open class RxRealmDataSource<E : Identified, D : RealmObject>(
        private val realmConfiguration: RealmConfiguration,
        private val dataMapper: RealmDataMapper<E, D>,
        private val queryMapper: RealmQueryMapper<D>
) : DataSource<E> {

    /**
     * [create] method is responsible for creation of the entities passed as [entity] param.
     * Method will trigger creation of the object in [Realm] database using [RealmQueryMapper]
     * to transform [entity] to [D] and [realmConfiguration] to open [Realm] instance and perform
     * synchronous write transaction. All async operation can be achieved using RxJava/RxKotlin tools
     * and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param entity of type [E] that contains object that should be created in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that will emit created object and complete event when
     * operation is done.
     */
    override fun create(entity: E): Observable<E> = executeLambdaForRealm { realm ->
        Observable.fromCallable { executeRewriteTransaction(entity, realm) }
    }

    /**
     * [update] method is responsible for updating of the entities passed as [entity] param.
     * Method will trigger updating of the object in [Realm] database using [RealmQueryMapper]
     * to transform [entity] to [D] and [realmConfiguration] to open [Realm] instance and perform
     * synchronous update transaction. All async operation can be achieved using RxJava/RxKotlin tools
     * and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param entity of type [E] that contains object that should be updated in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that will emit updated object and complete event when
     * operation is done.
     * @throws [ItemNotFoundException] exception when [QueryById] does not find the object in
     * the database with the same Id like [entity]
     */
    override fun update(entity: E): Observable<E> = executeLambdaForRealm { realm ->
        val item = query(realm, QueryById(entity.id)).first()
        if (item != null) {
            Observable.fromCallable { executeRewriteTransaction(entity, realm, item) }
        } else {
            Observable.error(ItemNotFoundException(entity))
        }
    }

    /**
     * [delete] method is responsible for deleting of the object that can be found using constraints
     * defined by [query] param. Method will trigger deleting of the object in [Realm] database using
     * [RealmQueryMapper] to transform [Query] object to [RealmQuery] and [realmConfiguration] to
     * open [Realm] instance and perform synchronous update transaction.  All async operation can
     * be achieved using RxJava/RxKotlin tools and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param query that contains constraints that defines what object should be deleted in DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that will emit complete event deleting is complete. Observable
     * **will not** emit any data entities.
     */
    override fun delete(query: Query): Observable<E> = executeLambdaForRealm { realm ->
        Completable.fromAction {
            realm.executeTransaction {
                query(realm, query).deleteAllFromRealm()
            }
        }.toObservable()
    }

    /**
     * [fetch] method is responsible for fetching all object that can be found using constraints
     * defined by [query] param. Method will trigger fetching of all objects that meet requirements
     * in [Realm] database using [RealmQueryMapper] to transform [Query] object to [RealmQuery]
     * and [realmConfiguration] to open [Realm] instance  All async operation can
     * be achieved using RxJava/RxKotlin tools and [Schedulers].
     *
     * [<br/><br/>] **Important** [<br/>]
     * The library does not provide any kind of logic for managing object Ids. [RepoLibRx] is responsible
     * for passing requests and its params to the data sources in order defined by the strategy. Data
     * wrapped in requests are passed without any interference or modification.
     *
     * [<br/><br/>]
     * @param query that contains constraints that defines what objects should be fetched from DataSource.
     * [<br/><br/>]
     * @return [Observable] of type [E] that emits all objects that matching [query].
     */
    override fun fetch(query: Query): Observable<E> = executeLambdaForRealm { realm ->
        Single.fromCallable { query(realm, query) }
                .filter { it.isLoaded }
                .filter { it.isValid }
                .map { realm.copyFromRealm(it) }
                .toObservable()
                .flatMapIterable { it }
                .map(dataMapper.transformToEntity())
    }

    private fun executeLambdaForRealm(realmAction: (Realm) -> Observable<E>)
            : Observable<E> = Observable.using(
            { Realm.getInstance(realmConfiguration) },
            realmAction,
            { realm -> realm.close() }
    )

    private fun executeRewriteTransaction(
            entity: E,
            realm: Realm,
            dao: D? = null
    ): E {
        var result = entity
        realm.executeTransaction {
            result = dataMapper.rewriteValuesToDao(entity, dao
                    ?: realm.createObject(queryMapper.daoClass))
        }
        return result
    }

    private fun query(realm: Realm, requestQuery: Query): RealmResults<D> = when (requestQuery) {
        is QueryAll -> queryMapper.transform(requestQuery, realm).findAll()
        is QueryWithParams -> queryMapper.transform(requestQuery, realm).findAll()
        is QueryById -> queryMapper.transform(requestQuery, realm).findAll()
        else -> queryMapper.transform(requestQuery, realm).findAll()
    }
}