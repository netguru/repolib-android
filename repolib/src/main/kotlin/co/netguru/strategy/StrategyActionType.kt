package co.netguru.strategy

import co.netguru.DataSource
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
        ): Flowable<T> = localDataSource.dataStream()
    }

    object OnlyRemote : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = remoteDataSource.dataStream()
    }

    object FirstLocalThenRemoteWithLocalUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = Flowable.merge(
                localDataSource.dataStream(),
                remoteDataSource.dataStream()
                        .flatMap { localDataSource.update(it).andThen(Flowable.just(it)) }
                        .mergeWith(localDataSource.dataStream())
        )
    }

    object FirstLocalThenRemoteNoLocalUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = Flowable.merge<T>(localDataSource.dataStream(), remoteDataSource.dataStream())
    }

    object FirstRemoteThenLocalWithLocalUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = remoteDataSource.dataStream()
                .flatMap { localDataSource.update(it).andThen(Flowable.just(it)) }
                .mergeWith(localDataSource.dataStream())
    }

    object FirstRemoteThenLocalNoUpdate : StrategyActionType() {
        override fun <T> mapToAction(
                localDataSource: DataSource<T>,
                remoteDataSource: DataSource<T>
        ): Flowable<T> = remoteDataSource.dataStream()
                .mergeWith(localDataSource.dataStream())
    }
}