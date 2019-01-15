package co.netguru.repolibrx.sample.feature.demo.datasource.api

import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*

interface API {

    @DELETE("sqlDelete")
    fun delete(@Query("id") id: Long): Completable

    @GET("getAll")
    fun get(): Observable<List<DemoDataEntity>>

    @POST("update")
    fun update(@Body entityToUpdate: DemoDataEntity): Observable<DemoDataEntity>

    @POST("create")
    fun create(@Body entityToCreate: DemoDataEntity): Observable<DemoDataEntity>
}