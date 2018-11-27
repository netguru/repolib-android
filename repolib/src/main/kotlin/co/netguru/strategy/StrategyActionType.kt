package co.netguru.strategy

import co.netguru.datasource.DataSource
import io.reactivex.Flowable

/**
 * Sealed class that contains predefined data flow's.
 * This data flows are used by to define strategy for specific data types
 */
sealed class StrategyActionType {

    abstract fun <T> mapToAction(
            localDataSource: DataSource<T>,
            remoteDataSource: DataSource<T>
    ): Flowable<T>

    object OnlyLocal : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = localDataSource.dataOutput()
    }

    object OnlyRemote : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = remoteDataSource.dataOutput()
    }

    object FirstLocalThenRemoteWithLocalUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = Flowable.merge(
                localDataSource.dataOutput(),
                remoteDataSource.dataOutput()
                        .flatMap { localDataSource.update(it).andThen(Flowable.just(it)) }
                        .mergeWith(localDataSource.dataOutput())
        )
    }

    object FirstLocalThenRemoteNoLocalUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = Flowable.merge<T>(
                localDataSource.dataOutput(), remoteDataSource.dataOutput()
        )
    }

    object FirstRemoteThenLocalWithLocalUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = remoteDataSource.dataOutput()
                .flatMap { localDataSource.update(it).andThen(Flowable.just(it)) }
                .mergeWith(localDataSource.dataOutput())
    }

    object FirstRemoteThenLocalNoUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = remoteDataSource.dataOutput()
                .mergeWith(localDataSource.dataOutput())
    }
}