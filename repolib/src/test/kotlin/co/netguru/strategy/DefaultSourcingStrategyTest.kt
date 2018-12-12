package co.netguru.strategy

import co.netguru.data.Request
import co.netguru.data.RequestType
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultSourcingStrategyTest {

    private val defaultStrategy = DefaultSourcingStrategy()

    @Test
    fun `for FETCH request return RemoteAndUpdateLocal`() {
        val request = Request<String>(RequestType.FETCH)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(StrategyType.Requests.RemoteAndUpdateLocal, result)
    }

    @Test
    fun `for CREATE request return OnlyRemote`() {
        val request = Request<String>(RequestType.CREATE)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(StrategyType.Requests.OnlyRemote, result)
    }

    @Test
    fun `for DELETE request return OnlyRemote`() {
        val request = Request<String>(RequestType.DELETE)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(StrategyType.Requests.OnlyRemote, result)
    }

    @Test
    fun `for UPDATE request return OnlyRemote`() {
        val request = Request<String>(RequestType.UPDATE)

        val result = defaultStrategy.select(request)

        Assert.assertEquals(StrategyType.Requests.OnlyRemote, result)
    }
}