package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.datasource.applyAdditionalAction
import co.netguru.repolibrx.datasource.asObservable
import io.reactivex.Observable

/**
 * Sealed class that contains predefined data flow's.
 * This data flows are used by to define requestsStrategy for specific data types
 */
sealed class RequestStrategy : Strategy {
    object OnlyLocal : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = localDataSource.applyAdditionalAction(dataSourceAction)
    }

    object OnlyRemote : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
    }

    object Both : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = Observable.merge(
                localDataSource.asObservable(),
                remoteDataSource.asObservable()
        ).flatMap(dataSourceAction)
    }

    object LocalAfterUpdateWithRemote : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
                .toList()
                .flatMapObservable {
                    localDataSource.delete(
//                            todo create abstraction for Query ALL
                            Request(RequestType.DELETE, query = object : Query<T>(null) {})
                    ).ignoreElements().andThen(Observable.fromIterable(it))
                }.flatMap {
                    localDataSource.create(Request(RequestType.CREATE, it))
                }.ignoreElements()
                .andThen(localDataSource.applyAdditionalAction(dataSourceAction))
    }

    object LocalOnRemoteFailure : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
                .onErrorResumeNext(localDataSource.applyAdditionalAction(dataSourceAction))
    }

    object LocalAfterUpdateOrFailureOfRemote : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = LocalAfterUpdateWithRemote.apply(localDataSource, remoteDataSource, dataSourceAction)
                .onErrorResumeNext(localDataSource.applyAdditionalAction(dataSourceAction))
    }
}