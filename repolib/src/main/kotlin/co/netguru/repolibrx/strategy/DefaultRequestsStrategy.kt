package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType

class DefaultRequestsStrategy(
        private val requestsStrategy: RequestStrategy = RequestStrategy.LocalAfterUpdateOrFailureOfRemote
) : RequestsStrategy {

    override fun <T> select(request: Request<T>): Strategy = if (request.type == RequestType.FETCH) {
        requestsStrategy
    } else {
        RequestStrategy.OnlyRemote
    }
}