package co.netguru.repolibrx.realmadapter

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmResults


class RxRealmDataSource<E : Identified, D : RealmObject>(
        private val realmConfiguration: RealmConfiguration,
        private val dataMapper: RealmDataMapper<E, D>,
        private val queryMapper: RealmQueryMapper<D>
) : DataSource<E> {

    override fun create(entity: E): Observable<E> = executeLambdaForRealm { realm ->
        Observable.fromCallable { executeRewriteTransaction(entity, realm) }
    }

    override fun update(entity: E): Observable<E> = executeLambdaForRealm { realm ->
        val item = query(realm, QueryById(entity.id)).first()
        if (item != null) {
            Observable.fromCallable { executeRewriteTransaction(entity, realm, item) }
        } else {
            Observable.error(ItemNotFoundException(entity))
        }
    }

    override fun delete(query: Query): Observable<E> = executeLambdaForRealm { realm ->
        Completable.fromAction {
            realm.executeTransaction {
                query(realm, query).deleteAllFromRealm()
            }
        }.toObservable()
    }

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

    companion object {
        const val UNDEFINED: Long = -1
    }
}