package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType

class DefaultRequestsStrategy(
        private val requestsStrategy: RequestStrategy = RequestStrategy.LocalAfterUpdateOrFailureOfRemote
) : RequestsStrategy {

    //todo refactor
    override fun <T> select(request: Request<T>): Strategy = when (request.type) {
        RequestType.CREATE -> RequestStrategy.OnlyRemote
        RequestType.DELETE -> RequestStrategy.Both
        RequestType.FETCH -> requestsStrategy
        RequestType.UPDATE -> RequestStrategy.OnlyRemote
    }
}