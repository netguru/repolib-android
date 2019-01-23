package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.datasource.applyAdditionalAction
import co.netguru.repolibrx.datasource.asObservable
import io.reactivex.Observable

/**
 * Sealed class that contains predefined [Strategy] implementations that controls data flow.
 * Implementations can be used in [RequestsStrategyFactory] to implement most common strategies for
 * request processing. Each strategy defines in what order, passed observable should be applied on passed
 * DataSources. [apply] method result [Observable] which is combination of actions applied on DataSources
 * in certain order. Passed action is an call of specific method/function on [DataSource] interface.
 */
sealed class RequestStrategy : Strategy {

    /**
     * [OnlyLocal] strategy defines that [RepoLib] will apply action (request)
     * only on **local** [DataSource].
     */
    object OnlyLocal : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = localDataSource.applyAdditionalAction(dataSourceAction)
    }

    /**
     * [OnlyRemote] strategy defines that [RepoLib] will apply action (request)
     * only on **remote** [DataSource].
     */
    object OnlyRemote : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
    }

    /**
     * [Both] strategy defines that [RepoLib] will apply action (request)
     * on **both** [DataSource]s - first on **local** then on **remote**.
     * Results of both will be merged.
     */
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

    /**
     * [LocalAfterFullUpdateWithRemote] strategy defines that [RepoLib] will apply action (request) on
     * remote [DataSource] and the result of action will fully replace data in local [DataSource]. When update is complete,
     * then data action will be applied on local DataSource.
     * Full replace means that all data entities in local DataSource will be deleted using delete(QueryAll).
     * Data will be removed when results will be downloaded from remote source to avoid failure of the source.
     * [<br/><br/>]
     * **This strategy is specially useful for fetch actions.**
     */
    object LocalAfterFullUpdateWithRemote : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
                .toList()
                .flatMapObservable {
                    localDataSource.delete(QueryAll)
                            .ignoreElements()
                            .andThen(Observable.fromIterable(it))
                }.flatMap {
                    localDataSource.create(it)
                }.ignoreElements()
                .andThen(localDataSource.applyAdditionalAction(dataSourceAction))
    }

    /**
     * [LocalOnRemoteFailure] strategy defines that [RepoLib] will apply action (request) on
     * remote [DataSource] first. When requests is executed with success then result from remote DataSource
     * is published to downstream. If remote DataSource return failure on request, then [RepoLib] will apply
     * action on local DataSource. Information about remote DataSource failure will be overwritten
     * by the results from the local DataSource request.
     * [<br/><br/>]
     * **This strategy is specially useful for fetch actions.**
     */
    object LocalOnRemoteFailure : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
                .onErrorResumeNext(localDataSource.applyAdditionalAction(dataSourceAction))
    }

    /**
     * [LocalAfterFullUpdateOrFailureOfRemote] is a combination of two other strategies: [LocalAfterFullUpdateWithRemote]
     * and [LocalOnRemoteFailure]. This strategy defines that [RepoLib] will apply action (request) on
     * remote [DataSource] first. When requests is executed with success then result from remote DataSource
     * replace the data in local DataSource. Then request is applied on local DataSource and results are published to downstream.
     * If remote DataSource return failure on request, then [RepoLib] will apply action only on local DataSource.
     * Information about remote DataSource failure will be overwritten by the results from the local DataSource request.
     * [<br/><br/>]
     * **This strategy is specially useful for fetch actions.**
     */
    object LocalAfterFullUpdateOrFailureOfRemote : RequestStrategy() {
        override fun <T> apply(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>,
                dataSourceAction: (DataSource<T>) -> Observable<T>
        ): Observable<T> = LocalAfterFullUpdateWithRemote.apply(localDataSource, remoteDataSource, dataSourceAction)
                .onErrorResumeNext(localDataSource.applyAdditionalAction(dataSourceAction))
    }
}