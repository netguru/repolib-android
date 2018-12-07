package co.netguru.strategy

import co.netguru.data.Request
import co.netguru.data.RequestType

class DefaultSourcingStrategy(
        private val requestsStrategy: StrategyType.Requests = StrategyType.Requests.RemoteAndUpdateLocal
) : SourcingStrategy {

    override fun <T> select(request: Request<T>) = if (request.type == RequestType.FETCH) {
        requestsStrategy
    } else {
        StrategyType.Requests.OnlyRemote
    }
}