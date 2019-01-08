package co.netguru.repolib.feature.demo.datasource.api

import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

//todo add JavaDoc description about model mapping
class RetrofitDataSource(private val api: API) : DataSource<DemoDataEntity> {

    override fun create(entity: DemoDataEntity): Observable<DemoDataEntity> = api.create(entity)
    override fun update(entity: DemoDataEntity): Observable<DemoDataEntity> = api.update(entity)

    override fun delete(query: Query)
            : Observable<DemoDataEntity> {
        return if (query is QueryWithParams) {
            api.delete(id = query.param("id")).toObservable()
        } else {
            Observable.error(UnsupportedOperationException("Unsupported query: $query"))
        }
    }

    override fun fetch(query: Query): Observable<DemoDataEntity> = api.get()
            .flatMap { Observable.fromIterable(it) }
}