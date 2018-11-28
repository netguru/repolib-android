package co.netguru

import co.netguru.strategy.SourcingStrategy
import co.netguru.strategy.StrategyType

class TestSourcingStrategy(
        val source: StrategyType.Source,
        val fetch: StrategyType.Fetch
) : SourcingStrategy {

    override fun outputStrategy() = source

    override fun fetchingStrategy(): StrategyType.Fetch = fetch
}