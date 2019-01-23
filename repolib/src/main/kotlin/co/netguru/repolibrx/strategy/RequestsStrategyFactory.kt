package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request

/**
 * Interface that represent factory for [Strategy]. Implement this interface with conditions checking for specific Requests.
 * [DefaultRequestsStrategyFactory] can be also used for simple cases to skipp implementation of the factory.
 */
interface RequestsStrategyFactory {

    /**
     * Implement this function with logic that will check which [Strategy] should be returned.
     *
     * @param request represent object related to the specific [Request].
     * It can be used to define rules or condition checks
     *
     * @return [Strategy] Strategy object that will be used to apply request on DataSources
     * in specific order. In most common cases predefined strategies from [RequestStrategy] can be used.
     * For more custom cases use own implementation of [Strategy]
     */
    fun <T> select(request: Request<T>): Strategy
}