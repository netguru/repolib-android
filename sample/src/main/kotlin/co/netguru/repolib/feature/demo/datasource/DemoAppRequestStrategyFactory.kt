package co.netguru.repolib.feature.demo.datasource

import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType
import co.netguru.repolibrx.strategy.RequestStrategy
import co.netguru.repolibrx.strategy.RequestsStrategy
import co.netguru.repolibrx.strategy.Strategy

class DemoAppRequestStrategyFactory : RequestsStrategy {

    override fun <T> select(request: Request<T>): Strategy = when (request.type) {
        RequestType.CREATE -> RequestStrategy.OnlyRemote
        RequestType.UPDATE -> RequestStrategy.OnlyRemote
        RequestType.DELETE -> RequestStrategy.OnlyRemote
        RequestType.FETCH -> RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote
    }
}