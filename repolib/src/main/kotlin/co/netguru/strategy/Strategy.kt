package co.netguru.strategy

import co.netguru.datasource.DataSource
import io.reactivex.Flowable

abstract class Strategy<T> {

    /**
     * Select data source publishing order based on type/condition
     * returned by selectScenarioForConditions(): StrategyActionType
     */
    fun selectDataOutput(
            localDataSource: DataSource<T>,
            remoteDataSource: DataSource<T>
    ): Flowable<T> = Flowable
            .fromCallable { selectScenarioForConditions() }
            .flatMap { it.mapToAction(localDataSource, remoteDataSource) }


    /**
     * Abstract method that should implement condition check for specific data type
     * and map condition to specific StrategyActionType
     */
    abstract fun selectScenarioForConditions(): StrategyActionType
}