package co.netguru

import co.netguru.data.Request
import co.netguru.strategy.SourcingStrategy
import co.netguru.strategy.StrategyType

class TestSourcingStrategy(
        private val requests: StrategyType.Requests
) : SourcingStrategy {


    override fun <T> select(request: Request<T>): StrategyType.Requests = requests
}