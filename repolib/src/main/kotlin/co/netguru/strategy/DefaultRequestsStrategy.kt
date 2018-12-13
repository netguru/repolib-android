package co.netguru.strategy

import co.netguru.data.Request
import co.netguru.data.RequestType

class DefaultRequestsStrategy(
        private val requestsStrategy: RequestStrategy = RequestStrategy
                .LocalAfterUpdateOrFailureOfRemote
) : RequestsStrategy {

    override fun <T> select(request: Request<T>): Strategy = if (request.type == RequestType.FETCH) {
        requestsStrategy
    } else {
        RequestStrategy.OnlyRemote
    }
}