package co.netguru.strategy

import co.netguru.data.Request

/**
 * Abstract class that should implement conditions checks for specific data type
 * and map condition to specific [RequestStrategy]
 */
interface RequestsStrategy {
    fun <T> select(request: Request<T>): Strategy
}