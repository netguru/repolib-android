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
            realm.where(DataDao::class.java)
                    .equalTo("id", request.query?.item?.id)
                    .findAll()
        }.doOnSuccess { item ->
            realm.executeTransaction {
                item.deleteAllFromRealm()
            }
        }.ignoreElement().toObservable<DemoDataEntity>()
    }

    override fun update(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = create(request).doOnSubscribe {
        executeLambdaForRealm { realm ->
            Single.fromCallable {
                realm.where(DataDao::class.java)
                        .findAll()
            }.doOnSuccess { item ->
                realm.executeTransaction {
                    item.deleteAllFromRealm()
                }
            }.ignoreElement().toObservable<DemoDataEntity>()
        }
    }


    //  todo to use it after refactor
    private fun executeLambdaForRealm(realmAction: (Realm) -> Observable<DemoDataEntity>)
            : Observable<DemoDataEntity> = Observable.using(
            { Realm.getInstance(realmConfiguration) },
            realmAction,
            { realm -> realm.close() }
    )
}