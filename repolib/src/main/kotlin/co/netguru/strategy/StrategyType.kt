package co.netguru.strategy

import co.netguru.data.Request
import co.netguru.data.RequestType
import co.netguru.datasource.DataSource
import co.netguru.datasource.applyAdditionalAction
import co.netguru.datasource.asFlowable
import io.reactivex.Flowable

/**
 * Sealed class that contains predefined data flow's.
 * This data flows are used by to define sourcingStrategy for specific data types
 */
sealed class StrategyType {

    abstract fun <T> apply(
            localDataSource: DataSource<T>,
            remoteDataSource: DataSource<T>,
            dataSourceAction: (DataSource<T>) -> Flowable<T>
    ): Flowable<T>

    abstract class Requests : StrategyType() {
        object OnlyLocal : Requests() {
            override fun <T> apply(
                    localDataSource: DataSource<T>,
                    remoteDataSource: DataSource<T>,
                    dataSourceAction: (DataSource<T>) -> Flowable<T>
            ): Flowable<T> = localDataSource.applyAdditionalAction(dataSourceAction)
        }

        object OnlyRemote : Requests() {
            override fun <T> apply(
                    localDataSource: DataSource<T>,
                    remoteDataSource: DataSource<T>,
                    dataSourceAction: (DataSource<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
        }

        //        todo refactor implementation
        object RemoteOnFailureLocal : Requests() {
            override fun <T> apply(
                    localDataSource: DataSource<T>,
                    remoteDataSource: DataSource<T>,
                    dataSourceAction: (DataSource<T>) -> Flowable<T>
            ): Flowable<T> = Flowable.merge(
                    localDataSource.asFlowable(),
                    remoteDataSource.asFlowable()
            ).flatMap(dataSourceAction)
        }

        object RemoteAndUpdateLocal : Requests() {
            override fun <T> apply(
                    localDataSource: DataSource<T>,
                    remoteDataSource: DataSource<T>,
                    dataSourceAction: (DataSource<T>) -> Flowable<T>
            ): Flowable<T> = remoteDataSource.applyAdditionalAction(dataSourceAction)
                    .flatMapCompletable {
                        localDataSource.update(Request(
                                type = RequestType.UPDATE,
                                entity = it
                        ))
                    }
                    .andThen(localDataSource.applyAdditionalAction(dataSourceAction))
        }
    }
}