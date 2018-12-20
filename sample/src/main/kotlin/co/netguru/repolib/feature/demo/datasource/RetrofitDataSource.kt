package co.netguru.repolib.feature.demo.datasource

import co.netguru.repolib.feature.demo.datasource.api.API
import co.netguru.repolib.feature.demo.di.DataEntity
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

class RetrofitDataSource(api: API) : DataSource<DataEntity> {

    override fun create(request: Request<DataEntity>): Observable<DataEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(request: Request<DataEntity>): Observable<DataEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fetch(request: Request<DataEntity>): Observable<DataEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(request: Request<DataEntity>): Observable<DataEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}