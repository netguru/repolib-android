package com.netguru.repolibrx.strategy

import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

/**
 * [Strategy] interface represents strategy that will by applied by the [RepoLib].
 * Strategy will execute specific request on [DataSource] in specific order. Most common cases
 * for DataSources synchronization are predefined in [RequestStrategy].
 */
interface Strategy {

    /**
     * [apply] function is used by main [RepoLib] to apply passed action (request) on
     * DataSources in specific order.
     *
     * @param localDataSource represents local [DataSource] object in the same way as it was defined in [RepoLib]
     * @param remoteDataSource represents local [DataSource] object in the same way as it was defined in [RepoLib]
     *
     * @return [Observable] that contains result of data stream combined into one Observable stream of data.
     * For most common cases return one of strategies predefined in [RequestStrategy] class.
     */
    fun <T> apply(
            localDataSource: DataSource<T>,
            remoteDataSource: DataSource<T>,
            dataSourceAction: (DataSource<T>) -> Observable<T>
    ): Observable<T>
}