package co.netguru.repolib.feature.demo.datasource.api

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

//todo add JavaDoc description about model mapping
class RetrofitDataSource(private val api: API) : DataSource<DemoDataEntity> {

    override fun fetch(request: Request.Fetch<DemoDataEntity>): Observable<DemoDataEntity> = api.get()
            .flatMap { Observable.fromIterable(it) }

    override fun create(request: Request.Create<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.create(request.entity)

    override fun delete(request: Request.Delete<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.delete(id = request.query.item!!.id).toObservable()

    override fun update(request: Request.Update<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.update(request.entity)
}