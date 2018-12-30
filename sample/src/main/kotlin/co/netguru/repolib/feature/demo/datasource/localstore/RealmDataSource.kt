package co.netguru.repolib.feature.demo.datasource.localstore

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.SourceType
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Flowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmConfiguration

//todo logic for all methods
class RealmDataSource(private val realmConfiguration: RealmConfiguration) : DataSource<DemoDataEntity> {

    private val daoToEntityMapperDemo: (DataDao) -> DemoDataEntity = {
        DemoDataEntity(it.id!!, it.value!!, SourceType.LOCAL)
    }

    override fun fetch(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        realm.where(DataDao::class.java)
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .flatMap { Flowable.fromIterable(it) }
                .cast(DataDao::class.java)
                .map(daoToEntityMapperDemo)
                .toObservable()
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
        realm.where(DataDao::class.java)
                .equalTo("id", request.entity?.id)
                .findAll()
                .asFlowable()
                .doOnNext { item ->
                    realm.executeTransaction {
                        item.deleteAllFromRealm()
                    }
                }.toObservable()
                .flatMap { Observable.fromIterable(it) }
                .map(daoToEntityMapperDemo)
    }

    override fun update(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = executeLambdaForRealm { realm ->
        Observable.fromIterable(
                realm.where(DataDao::class.java)
                        .equalTo("id", request.entity?.id)
                        .findAll()
        )
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .map { item ->
                    realm.executeTransaction {
                        item.apply {
                            id = request.entity?.id
                            value = request.entity?.value
                        }
                    }
                    item
                }.map(daoToEntityMapperDemo)
                .switchIfEmpty(create(request))
    }

    //  todo to use it after refactor
    private fun executeLambdaForRealm(realmAction: (Realm) -> Observable<DemoDataEntity>)
            : Observable<DemoDataEntity> = Observable.using(
            { Realm.getInstance(realmConfiguration) },
            realmAction,
            { realm -> realm.close() }
    )
}