package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultRequestsStrategyFactoryFactoryTest {

    private val defaultStrategy = DefaultRequestsStrategyFactoryFactory()

    @Test
    fun `for FETCH request return default strategy - RemoteAndUpdateLocal`() {
        val request = Request<String>(RequestType.FETCH)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote, result)
    }

    @Test
    fun `for CREATE request return OnlyRemote`() {
        val request = Request<String>(RequestType.CREATE)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.OnlyRemote, result)
    }

    @Test
    fun `for DELETE request return OnlyRemote`() {
        val request = Request<String>(RequestType.DELETE)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.OnlyRemote, result)
    }

    @Test
    fun `for UPDATE request return OnlyRemote`() {
        val request = Request<String>(RequestType.UPDATE)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.OnlyRemote, result)
    }
}