package com.netguru.repolibrx.sample.feature.demo.datasource.api

import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.QueryWithParams
import com.netguru.repolibrx.datasource.DataSource
import com.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.netguru.repolibrx.sample.feature.demo.di.datasources.RetrofitModule
import io.reactivex.Observable
import retrofit2.Retrofit

/**
 * [RetrofitDataSource] is example implementation of the remote [DataSource] based on [Retrofit]
 * framework. It contains logic of mapping data entities or queries to the data required by the
 * REST API interface.
 *
 * @param api example of REST API interface created  by the [Retrofit]. Initialization of this
 * interface and whole Retrofit is placed in the [RetrofitModule].
 */
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