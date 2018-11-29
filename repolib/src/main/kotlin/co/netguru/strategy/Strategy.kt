package co.netguru.strategy

/**
 * Abstract class that should implement conditions check for specific data type
 * and map condition to specific [StrategyType]
 */
interface Strategy {
    fun outputStrategy(): StrategyType.SourceStrategy
    fun fetchingStrategy(): StrategyType.FetchingStrategy
}