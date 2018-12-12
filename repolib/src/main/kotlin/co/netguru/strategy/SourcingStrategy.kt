package co.netguru.strategy

import co.netguru.data.Request

/**
 * Abstract class that should implement conditions check for specific data type
 * and map condition to specific [StrategyType]
 */
interface SourcingStrategy {
    fun <T> select(request: Request<T>): StrategyType.Requests
}