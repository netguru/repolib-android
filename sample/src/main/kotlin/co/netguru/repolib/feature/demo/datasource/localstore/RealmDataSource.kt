package co.netguru.repolib.feature.demo.datasource.localstore

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.SourceType
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration

class RealmDataSource(private val realmConfiguration: RealmConfiguration) : DataSource<DemoDataEntity> {

    private val daoToEntityMapperDemo: (DataDao) -> DemoDataEntity = {
        DemoDataEntity(it.id!!, it.value!!, SourceType.LOCAL)
    }

    override fun fetch(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Single.fromCallable {
            realm.where(DataDao::class.java)
                    .findAll()
        }.filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .toObservable()
                .flatMap { Observable.fromIterable(it) }
                .cast(DataDao::class.java)
                .map(daoToEntityMapperDemo)
    }

    override fun create(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Observable.fromCallable {
            val entityDemo: DemoDataEntity? = request.entity
            realm.executeTransaction {
                realm.createObject(DataDao::class.java)
                        .apply {
                            id = entityDemo?.id
                            value = entityDemo?.value
                        }
            }
            entityDemo
        }
    }

    override fun delete(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Single.fromCallable {
            val query = realm.where(DataDao::class.java)
            if (request.query?.item != null) {
//                todo reimplement query all
                query.equalTo("id", request.query?.item?.id)
            }
            query.findAll()
        }.doOnSuccess { item ->
            realm.executeTransaction {
                item.deleteAllFromRealm()
            }
        }.ignoreElement().toObservable<DemoDataEntity>()
    }

    override fun update(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Single.fromCallable {
            val item = realm.where(DataDao::class.java)
                    .equalTo("id", request.query?.item?.id)
                    .findFirst()
            realm.executeTransaction { item?.value = request.query?.item?.value }
            item
        }.toObservable().map(daoToEntityMapperDemo)

    }

    //  todo to use it after refactor
    private fun executeLambdaForRealm(realmAction: (Realm) -> Observable<DemoDataEntity>)
            : Observable<DemoDataEntity> = Observable.using(
            { Realm.getInstance(realmConfiguration) },
            realmAction,
            { realm -> realm.close() }
    )
}