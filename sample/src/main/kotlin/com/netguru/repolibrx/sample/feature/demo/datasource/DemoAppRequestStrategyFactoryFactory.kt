package com.netguru.repolibrx.sample.feature.demo.datasource

import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.data.Request
import com.netguru.repolibrx.strategy.RequestStrategy
import com.netguru.repolibrx.strategy.RequestsStrategyFactory
import com.netguru.repolibrx.strategy.Strategy

/**
 * [DemoAppRequestStrategyFactoryFactory] is an example implementation of the [RequestsStrategyFactory]
 * used by the [RepoLib] to select [Strategy] for different requests.
 */
class DemoAppRequestStrategyFactoryFactory : RequestsStrategyFactory {

    override fun <T> select(request: Request<T>): Strategy = when (request) {
        is Request.Create -> RequestStrategy.OnlyRemote
        is Request.Update -> RequestStrategy.OnlyRemote
        is Request.Delete -> RequestStrategy.OnlyRemote
        is Request.Fetch -> RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote
    }
}