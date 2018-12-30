package co.netguru.repolib.feature.demo.datasource.api

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.SourceType
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

//todo add mapping from API MODELS to local models
class RetrofitDataSource(private val api: API) : DataSource<DemoDataEntity> {

    private val remoteToLocal: (RemoteDataEntity) -> DemoDataEntity = {
        DemoDataEntity(it.id, it.note, SourceType.REMOTE)
    }

    private val localToRemote: (DemoDataEntity?) -> RemoteDataEntity = {
        if (it != null) {
            RemoteDataEntity(it.id, it.value)
        } else {
            throw UnsupportedOperationException("entity is null")
        }
    }

    override fun fetch(request: Request<DemoDataEntity>): Observable<DemoDataEntity> = api.get()
            .flatMap { Observable.fromIterable(it) }.map(remoteToLocal)

    override fun create(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.create(localToRemote(request.entity)).map(remoteToLocal)

    override fun delete(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.delete(localToRemote(request.query?.item).id)
            .andThen(Observable.just(request.query?.item!!))

    override fun update(request: Request<DemoDataEntity>)
            : Observable<DemoDataEntity> = api.update(localToRemote(request.entity)).map(remoteToLocal)
}