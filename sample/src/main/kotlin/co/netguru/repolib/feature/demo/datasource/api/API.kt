package co.netguru.repolib.feature.demo.datasource.api

import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.GET

interface API {
    fun delete(): Completable

    //    todo refactor
    @GET("getAll")
    fun get(): Observable<List<RemoteDataEntity>>

    fun update(): Observable<RemoteDataEntity>
    fun create(): Observable<RemoteDataEntity>
}