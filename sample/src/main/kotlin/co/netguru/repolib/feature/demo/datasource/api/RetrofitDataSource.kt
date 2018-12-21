package co.netguru.repolib.feature.demo.datasource.api

import co.netguru.repolib.feature.demo.di.DataEntity
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

class RetrofitDataSource(api: API) : DataSource<DataEntity> {

    override fun create(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(1, "create remote")
    }

    override fun delete(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(2, "delete remote")
    }

    override fun fetch(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(3, "fetch remote")
    }

    override fun update(request: Request<DataEntity>): Observable<DataEntity> = Observable.fromCallable {
        DataEntity(4, "update remote")
    }
}