package co.netguru.repolib.feature.demo.datasource.api

import io.reactivex.Completable
import io.reactivex.Observable

interface API {
    fun delete(): Completable

    //    todo refactor
    fun get(): Observable<List<RemoteDataEntity>>

    fun update(): Observable<RemoteDataEntity>
    fun create(): Observable<RemoteDataEntity>
}