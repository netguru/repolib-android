package co.netguru.strategy

/**
 * Test implementation of the Strategy
 */
class TestStrategy(private val strategyActionType: StrategyActionType) : Strategy<String>() {
    override fun selectScenarioForConditions() = strategyActionType
}