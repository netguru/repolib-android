package co.netguru.repolib.feature.demo.datasource.api

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.SourceType
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

//todo add mapping from API MODELS to local models
class RetrofitDataSource(private val api: API) : DataSource<DemoDataEntity> {

    private val mapper: (RemoteDataEntity) -> DemoDataEntity = {
        DemoDataEntity(it.id, it.note, SourceType.REMOTE)
    }

    override fun fetch(request: Request<DemoDataEntity>): Observable<DemoDataEntity> = api.get()
            .flatMap { Observable.fromIterable(it) }.map(mapper)

    override fun create(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.create().map(mapper)

    override fun delete(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.delete().toObservable()

    override fun update(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.update().map(mapper)
}