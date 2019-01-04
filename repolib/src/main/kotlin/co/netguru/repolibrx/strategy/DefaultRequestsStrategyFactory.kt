package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request

class DefaultRequestsStrategyFactory(
        private val requestsStrategy: RequestStrategy = RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote
) : RequestsStrategyFactory {

    override fun <T> select(request: Request<T>): Strategy = if (request is Request.Fetch) {
        requestsStrategy
    } else {
        RequestStrategy.OnlyRemote
    }
}