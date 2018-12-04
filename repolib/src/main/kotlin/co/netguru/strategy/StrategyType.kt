package co.netguru.strategy

import co.netguru.datasource.DataSourceController
import co.netguru.datasource.applyAdditionalAction
import co.netguru.datasource.asFlowable
import io.reactivex.Flowable

/**
 * Sealed class that contains predefined data flow's.
 * This data flows are used by to define strategy for specific data types
 */
sealed class StrategyType {

    abstract fun <T> applyStrategy(
            localDataSource: DataSourceController<T>,
            remoteDataSource: DataSourceController<T>,
            dataSourceAction: (DataSourceController<T>) -> Flowable<T>
    ): Flowable<T>

    abstract class FetchingStrategy : StrategyType() {
        object OnlyLocal : FetchingStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = localDataSource.applyAdditionalAction(dataSourceAction)
        }

        object OnlyRemote : FetchingStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
        }

        object Both : FetchingStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = Flowable.merge(
                    localDataSource.asFlowable(),
                    remoteDataSource.asFlowable()
            ).flatMap(dataSourceAction)
        }
    }

    abstract class SourceStrategy : StrategyType() {

        object Local : SourceStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = localDataSource.dataOutput()
        }

        object Remote : SourceStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.dataOutput()
        }

        object Merge : SourceStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = Flowable.merge(
                    localDataSource.dataOutput(),
                    remoteDataSource.dataOutput()
            )
        }

        object EmitLocalOnRemoteFailure : SourceStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.dataOutput()
                    .onErrorResumeNext(localDataSource.dataOutput())
        }

        object EmitLocalUpdatedByPrimary : SourceStrategy() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.dataOutput()
                    .flatMapCompletable { localDataSource.update(it) }
                    .andThen(localDataSource.dataOutput())
        }
    }
}