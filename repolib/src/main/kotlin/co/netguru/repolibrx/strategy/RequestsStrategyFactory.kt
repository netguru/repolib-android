package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request

/**
 * Abstract class that should implement conditions checks for specific data type
 * and map condition to specific [RequestStrategy]
 */
interface RequestsStrategyFactory {
    fun <T> select(request: Request<T>): Strategy
}