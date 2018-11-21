package co.netguru

import co.netguru.strategy.Strategy
import io.reactivex.Flowable


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val strategy: Strategy<T>
) {

    fun resource(): Flowable<T> {
        return strategy.selectDataOutput(localDataSource, remoteDataSource)
    }
}