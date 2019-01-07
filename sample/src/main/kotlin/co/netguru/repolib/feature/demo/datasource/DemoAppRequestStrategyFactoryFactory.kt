package co.netguru.repolib.feature.demo.datasource

import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.strategy.RequestStrategy
import co.netguru.repolibrx.strategy.RequestsStrategyFactory
import co.netguru.repolibrx.strategy.Strategy

class DemoAppRequestStrategyFactoryFactory : RequestsStrategyFactory {

    override fun <T> select(request: Request<T>): Strategy = when (request) {
        is Request.Create -> RequestStrategy.OnlyRemote
        is Request.Update -> RequestStrategy.OnlyRemote
        is Request.Delete -> RequestStrategy.OnlyRemote
        is Request.Fetch -> RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote
    }
}