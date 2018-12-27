package co.netguru.repolib.feature.demo.datasource.localstore

import co.netguru.repolib.feature.demo.di.DataEntity
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmConfiguration

//todo logic for all methods
class RealmDataSource(private val realmConfiguration: RealmConfiguration) : DataSource<DataEntity> {

    override fun create(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(1, "create local")
    }

    override fun delete(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(2, "delete local")
    }

    override fun fetch(request: Request<DataEntity>): Observable<DataEntity> = Observable
            .using(
                    { Realm.getInstance(realmConfiguration) },
                    { realm ->
                        Observable.fromCallable { DataEntity(3) }
                    },
                    { realm -> realm.close() }
            )

    override fun update(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(4, "update local")
    }
}