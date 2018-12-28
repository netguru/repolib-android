package co.netguru.repolib.feature.demo.datasource.api

import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface API {

    @DELETE("delete")
    fun delete(): Completable

    //    todo refactor
    @GET("getAll")
    fun get(): Observable<List<RemoteDataEntity>>

    @POST("update")
    fun update(): Observable<RemoteDataEntity>

    @POST("create")
    fun create(): Observable<RemoteDataEntity>
}