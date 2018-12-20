package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.datasource.DataSource
import io.reactivex.Observable

interface Strategy {

    fun <T> apply(
            localDataSource: DataSource<T>,
            remoteDataSource: DataSource<T>,
            dataSourceAction: (DataSource<T>) -> Observable<T>
    ): Observable<T>
}