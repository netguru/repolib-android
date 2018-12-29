package co.netguru.repolib.feature.demo.datasource.api

import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.*

interface API {

    @DELETE("delete")
    fun delete(@Query("id") id: Long): Completable

    //    todo refactor
    @GET("getAll")
    fun get(): Observable<List<RemoteDataEntity>>

    @POST("update")
    fun update(@Body entityToUpdate: RemoteDataEntity): Observable<RemoteDataEntity>

    @POST("create")
    fun create(@Body entityToCreate: RemoteDataEntity): Observable<RemoteDataEntity>
}