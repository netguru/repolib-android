package co.netguru.strategy

import co.netguru.datasource.DataSourceController
import co.netguru.datasource.applyAdditionalAction
import co.netguru.datasource.asFlowable
import io.reactivex.Flowable

/**
 * Sealed class that contains predefined data flow's.
 * This data flows are used by to define sourcingStrategy for specific data types
 */
sealed class StrategyType {

    abstract fun <T> applyStrategy(
            localDataSource: DataSourceController<T>,
            remoteDataSource: DataSourceController<T>,
            dataSourceAction: (DataSourceController<T>) -> Flowable<T>
    ): Flowable<T>

    abstract class Fetch : StrategyType() {
        object OnlyLocal : Fetch() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = localDataSource.applyAdditionalAction(dataSourceAction)
        }

        object OnlyRemote : Fetch() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
        }

        object Both : Fetch() {
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

    abstract class Source : StrategyType() {

        object Local : Source() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = localDataSource.dataOutput()
        }

        object Remote : Source() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.dataOutput()
        }

        object Merge : Source() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = Flowable.merge(
                    localDataSource.dataOutput(),
                    remoteDataSource.dataOutput()
            )
        }

        object EmitLocalOnRemoteFailure : Source() {
            override fun <T> applyStrategy(
                    localDataSource: DataSourceController<T>,
                    remoteDataSource: DataSourceController<T>,
                    dataSourceAction: (DataSourceController<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.dataOutput()
                    .onErrorResumeNext(localDataSource.dataOutput())
        }

        object EmitLocalUpdatedByPrimary : Source() {
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