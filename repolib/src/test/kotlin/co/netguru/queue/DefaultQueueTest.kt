package co.netguru.queue

import co.netguru.data.Request
import co.netguru.data.RequestType
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert
import org.junit.Test

class DefaultQueueTest {

    private val mapMock: MutableMap<Int, Request<String>> = mock()
    private val queueTest = DefaultQueue(mapMock)
    private val requestMock = Request<String>(RequestType.UPDATE)

    @Test
    fun `when ADD called then call add on map`() {

        queueTest.add(requestMock)

        verify(mapMock)[requestMock.hashCode()] = requestMock
    }

    @Test
    fun `when REMOVE called then call add on map`() {

        queueTest.remove(requestMock)

        verify(mapMock).remove(requestMock.hashCode())
    }

    @Test
    fun `when isEmpty is called then call isEmpty on map and return its value`() {
        val expected = true
        whenever(mapMock.isEmpty()).thenReturn(expected)

        val result = queueTest.isEmpty()

        verify(mapMock).isEmpty()
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `when getAllRequests is called then return all values`() {
        val expected = mutableListOf<Request<String>>()
        whenever(mapMock.values).thenReturn(expected)

        val result = queueTest.getAllRequests()

        Assert.assertEquals(expected, result)
        verify(mapMock).values
    }
}