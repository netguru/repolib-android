package co.netguru.repolib.feature.demo.datasource.localstore

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.SourceType
import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults

class RealmDataSource(private val realmConfiguration: RealmConfiguration) : DataSource<DemoDataEntity> {

    private val daoToEntityMapperDemo: (DataDao) -> DemoDataEntity = {
        DemoDataEntity(it.id!!, it.value!!, SourceType.LOCAL)
    }

    override fun fetch(query: Query)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Single.fromCallable { query(realm, query) }
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .toObservable()
                .flatMap { Observable.fromIterable(it) }
                .cast(DataDao::class.java)
                .map(daoToEntityMapperDemo)
    }

    override fun create(entity: DemoDataEntity)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Observable.fromCallable {
            val entityDemo: DemoDataEntity = entity
            realm.executeTransaction {
                realm.createObject(DataDao::class.java)
                        .apply {
                            id = entityDemo.id
                            value = entityDemo.value
                        }
            }
            entityDemo
        }
    }

    override fun delete(query: Query)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Single.fromCallable { query(realm, query) }
                .doOnSuccess { item ->
                    realm.executeTransaction {
                        item.deleteAllFromRealm()
                    }
                }.ignoreElement().toObservable<DemoDataEntity>()
    }

    override fun update(entity: DemoDataEntity)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Single.fromCallable {
            val item = realm.where(DataDao::class.java)
                    .equalTo("id", entity.id)
                    .findFirst()
            realm.executeTransaction { item?.value = entity.value }
            item
        }.toObservable().map(daoToEntityMapperDemo)

    }

    private fun executeLambdaForRealm(realmAction: (Realm) -> Observable<DemoDataEntity>)
            : Observable<DemoDataEntity> = Observable.using(
            { Realm.getInstance(realmConfiguration) },
            realmAction,
            { realm -> realm.close() }
    )

    private fun query(realm: Realm, requestQuery: Query): RealmResults<DataDao> = when (requestQuery) {
        is QueryWithParams -> {
            val paramName = "id"
            realm.where(DataDao::class.java)
                    .equalTo(paramName, requestQuery.param<String>(paramName))
                    .findAll()
        }
        is QueryAll -> realm.where(DataDao::class.java).findAll()
        else -> realm.where(DataDao::class.java).findAll()
    }
}