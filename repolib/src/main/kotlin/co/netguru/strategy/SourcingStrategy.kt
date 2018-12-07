package co.netguru.strategy

/**
 * Abstract class that should implement conditions check for specific data type
 * and map condition to specific [StrategyType]
 */
interface SourcingStrategy {
    fun outputStrategy(): StrategyType.Source
    fun fetchingStrategy(): StrategyType.Fetch
}