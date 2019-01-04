package co.netguru.repolibrx.strategy

import co.netguru.repolibrx.data.Request
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultRequestsStrategyFactoryTest {

    private val defaultStrategy = DefaultRequestsStrategyFactory()

    @Test
    fun `for FETCH request return default strategy - RemoteAndUpdateLocal`() {
        val request = Request.Fetch<String>(mock())

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote, result)
    }

    @Test
    fun `for CREATE request return OnlyRemote`() {
        val request = Request.Create("")

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.OnlyRemote, result)
    }

    @Test
    fun `for DELETE request return OnlyRemote`() {
        val request = Request.Delete<String>(mock())

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.OnlyRemote, result)
    }

    @Test
    fun `for UPDATE request return OnlyRemote`() {
        val request = Request.Update("")

        val result = defaultStrategy.select(request)

        Assert.assertEquals(RequestStrategy.OnlyRemote, result)
    }
}