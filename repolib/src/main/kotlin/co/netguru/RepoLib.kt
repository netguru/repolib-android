package co.netguru

import co.netguru.datasource.DataSource
import co.netguru.datasource.Query
import co.netguru.strategy.Resource
import co.netguru.strategy.Strategy
import io.reactivex.Flowable


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val strategy: Strategy<T>
) {

    fun fetch(query: Query<T>): Resource<T> {
        return object : Resource<T>() {
            override fun flowable(): Flowable<T> {
                return strategy.selectDataOutput(localDataSource, remoteDataSource)
            }

        }
    }
}