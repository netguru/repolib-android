package co.netguru

import co.netguru.datasource.DataSource
import co.netguru.datasource.Query
import co.netguru.strategy.Strategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable


class RepoLib<T>(
        private val localDataSource: DataSource<T>,
        private val remoteDataSource: DataSource<T>,
        private val strategy: Strategy
) {

    /**
     * Output stream that is responsible for transmitting data from the data sources.
     * Data emission is triggered by the input event sent using one of the input methods
     * e.g. [fetch].
     * [<br><br>]
     * Source for the data emission is selected by the Strategy object
     *
     */
    fun outputDataStream(): Flowable<T> = Flowable.just(strategy.outputStrategy())
            .flatMap { strategyType ->
                strategyType.applyStrategy(
                        localDataSource,
                        remoteDataSource
                ) { it.dataOutput() }
            }

    fun fetch(query: Query<T>) = Flowable.just(strategy.fetchingStrategy())
            .flatMap { strategyType ->
                strategyType.applyStrategy(
                        localDataSource,
                        remoteDataSource
                ) { it.fetch(query).toFlowable() }
            }

    fun delete(query: Query<T>) = applyFunction(query.sourceType) { it.delete(query) }

    fun update(entity: T) = applyFunction { it.update(entity) }

    fun create(entity: T) = applyFunction { it.create(entity) }


    private fun applyFunction(
            sourceType: TargetType = TargetType.REMOTE,
            function: (DataSource<T>) -> Completable): Completable = when (sourceType) {
        TargetType.LOCAL -> Observable.just(localDataSource).flatMapCompletable(function)
        TargetType.REMOTE -> Observable.just(remoteDataSource).flatMapCompletable(function)
        TargetType.BOTH -> {
            Observable.fromArray(localDataSource, remoteDataSource).flatMapCompletable(function)
        }
    }
}