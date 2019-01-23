package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.Request.Fetch

/**
 * Class represents Default strategy factory that can be used for simple cases. It returns [RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote]
 * for [Fetch] requests. For all other requests factory will return [RequestStrategy.OnlyRemote].
 * Default action for [Fetch] request can be overwritten by injecting other [RequestStrategy] through the constructor
 */
class DefaultRequestsStrategyFactory(
        private val requestsStrategy: RequestStrategy = RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote
) : RequestsStrategyFactory {

    override fun <T> select(request: Request<T>): Strategy = if (request is Request.Fetch) {
        requestsStrategy
    } else {
        RequestStrategy.OnlyRemote
    }
}