package co.netguru.repolib.feature.demo.datasource.localstore

import co.netguru.repolib.feature.demo.data.DataEntity
import co.netguru.repolib.feature.demo.data.SourceType
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Flowable
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmConfiguration

//todo logic for all methods
class RealmDataSource(private val realmConfiguration: RealmConfiguration) : DataSource<DataEntity> {

    private val daoToEntityMapper: (DataDao) -> DataEntity = {
        DataEntity(it.id!!, it.value!!, SourceType.LOCAL)
    }

    override fun fetch(request: Request<DataEntity>): Observable<DataEntity> = executeLambdaForRealm { realm ->
        realm.where(DataDao::class.java)
                .findAllAsync()
                .asFlowable()
                .filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .flatMap { Flowable.fromIterable(it) }
                .cast(DataDao::class.java)
                .map(daoToEntityMapper)
                .toObservable()
    }

    override fun create(request: Request<DataEntity>): Observable<DataEntity> = executeLambdaForRealm { realm ->
        Observable.fromCallable {
            val entity: DataEntity? = request.entity
            realm.executeTransaction {
                realm.createObject(DataDao::class.java)
                        .apply {
                            id = entity?.id
                            value = entity?.value
                        }

            }
            entity
        }
    }

    override fun delete(request: Request<DataEntity>): Observable<DataEntity> = executeLambdaForRealm { realm ->
        realm.where(DataDao::class.java)
                .findAllAsync()
                .asFlowable()
                .doOnNext { item ->
                    realm.executeTransaction {
                        item.deleteAllFromRealm()
                    }
                }.toObservable()
                .flatMap { Observable.fromIterable(it) }
                .map(daoToEntityMapper)
    }

    override fun update(request: Request<DataEntity>): Observable<DataEntity> = executeLambdaForRealm { realm ->
        Observable.fromCallable {
            realm.where(DataDao::class.java)
                    .findFirst()
        }.filter { it.isLoaded }
                .map { realm.copyFromRealm(it) }
                .map { item ->
                    realm.executeTransaction {
                        item.apply {
                            id = request.entity?.id
                            value = request.entity?.value
                        }
                    }
                    item
                }.map(daoToEntityMapper)
    }

    //  todo to use it after refactor
    private fun executeLambdaForRealm(realmAction: (Realm) -> Observable<DataEntity>) = Observable.using(
            { Realm.getInstance(realmConfiguration) },
            realmAction,
            { realm -> realm.close() }
    )
}