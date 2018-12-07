package co.netguru.datasource

import co.netguru.TestDataEntity
import co.netguru.cache.RequestQueue
import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RemoteDataSourceControllerTest {

    private val testDataEntity = TestDataEntity(99)
    private val query: Query<TestDataEntity> = mock()
    private val captor = argumentCaptor<Request<TestDataEntity>>()
    private val list = mutableListOf(testDataEntity)

    private val requestQueueMock: RequestQueue<TestDataEntity> = mock {}
    private val dataSource: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(list)
        on { delete(any()) } doReturn Completable.complete()
        on { create(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    @Spy
    private val cacheDataSource = RemoteDataSourceController(
            dataSource,
            requestQueueMock
    )

    @Test
    fun `when fetch() is subscribed then call fetch() and return data in output without Queue interaction`() {

        val testSubscriber = cacheDataSource.fetch(query).test()
        val dataSubscriber = cacheDataSource.dataOutput().test()

        testSubscriber.assertComplete()
        dataSubscriber.assertValues(testDataEntity)
        verify(dataSource).fetch(captor.capture())
        val result = captor.firstValue
        Assert.assertEquals(query, result.query)
        Assert.assertEquals(RequestType.FETCH, result.requestType)
        verify(requestQueueMock, never()).remove(result)
        verify(requestQueueMock, never()).add(any())
    }

    @Test
    fun `when fetch() is subscribed and throw exception then call query() and return exception on both streams without Queue interaction`() {
        val error = Throwable("test")
        whenever(dataSource.fetch(any())).thenReturn(Flowable.error(error))

        val testSubscriber = cacheDataSource.fetch(query).test()
        val dataSubscriber = cacheDataSource.dataOutput().test()

        testSubscriber.assertError(error)
        dataSubscriber.assertError(error)
        verify(dataSource).fetch(captor.capture())
        verify(requestQueueMock, never()).add(any())
        val result = captor.firstValue
        Assert.assertEquals(query, result.query)
        Assert.assertEquals(RequestType.FETCH, result.requestType)
        verify(requestQueueMock, never()).remove(result)
    }

    @Test
    fun `when create() subscribed and request is possible with empty cache then call create()`() {
        whenever(requestQueueMock.isEmpty()).thenReturn(true)

        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(requestQueueMock, never()).add(any())
        verify(requestQueueMock).isEmpty()
        verify(dataSource).create(captor.capture())
        Assert.assertEquals(RequestType.CREATE, captor.firstValue.requestType)
        Assert.assertEquals(testDataEntity, captor.firstValue.entity)
        Assert.assertNull(captor.firstValue.query)
    }

    @Test
    fun `when create() is subscribed and cache is not empty then execute all request`() {
        val cachedRequest = listOf<Request<TestDataEntity>>(
                Request(RequestType.UPDATE),
                Request(RequestType.DELETE),
                Request(RequestType.DELETE),
                Request(RequestType.CREATE)
        )
        whenever(requestQueueMock.isEmpty()).thenReturn(false)
        whenever(requestQueueMock.getAllRequests()).thenReturn(cachedRequest)
        val removeCaptor = argumentCaptor<Request<TestDataEntity>>()

        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(requestQueueMock).isEmpty()
        verify(requestQueueMock).getAllRequests()
        //Argument captor makes order inconsistent
        // - in normal use case order of the requests will be equal to initial order of method calls
        verify(dataSource).update(captor.capture())
        verify(dataSource, times(2)).delete(captor.capture())
        verify(dataSource, times(2)).create(captor.capture())
        Assert.assertEquals(RequestType.UPDATE, captor.allValues[0].requestType)
        Assert.assertEquals(RequestType.DELETE, captor.allValues[1].requestType)
        Assert.assertEquals(RequestType.DELETE, captor.allValues[2].requestType)
        Assert.assertEquals(RequestType.CREATE, captor.allValues[3].requestType)
        Assert.assertEquals(RequestType.CREATE, captor.allValues[4].requestType)
        verify(requestQueueMock, times(cachedRequest.size + 1)).remove(removeCaptor.capture())
        Assert.assertEquals(captor.allValues[0].requestType, removeCaptor.allValues[0].requestType)
        Assert.assertEquals(captor.allValues[1].requestType, removeCaptor.allValues[1].requestType)
        Assert.assertEquals(captor.allValues[2].requestType, removeCaptor.allValues[2].requestType)
        Assert.assertEquals(captor.allValues[3].requestType, removeCaptor.allValues[3].requestType)
        Assert.assertEquals(captor.allValues[4].requestType, removeCaptor.allValues[4].requestType)
    }

    @Test
    fun `when update() is subscribed and cache contains FETCH request then execute all request and emit data`() {
        list.add(testDataEntity.copy(id = 33))
        list.add(testDataEntity.copy(id = 44))
        val cachedRequest = listOf<Request<TestDataEntity>>(
                Request(RequestType.FETCH),
                Request(RequestType.UPDATE),
                Request(RequestType.DELETE),
                Request(RequestType.DELETE),
                Request(RequestType.CREATE)
        )
        whenever(requestQueueMock.isEmpty()).thenReturn(false)
        whenever(requestQueueMock.getAllRequests()).thenReturn(cachedRequest)
        val removeCaptor = argumentCaptor<Request<TestDataEntity>>()

        val dataSubscriber = cacheDataSource.dataOutput().test()
        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(requestQueueMock).isEmpty()
        verify(requestQueueMock).getAllRequests()
        verify(dataSource).fetch(captor.capture())
        verify(dataSource).update(captor.capture())
        verify(dataSource, times(2)).delete(captor.capture())
        dataSubscriber.assertValueCount(list.size)
        dataSubscriber.assertValues(list[0], list[1], list[2])
        verify(dataSource, times(2)).create(captor.capture())
        Assert.assertEquals(RequestType.FETCH, captor.allValues[0].requestType)
        Assert.assertEquals(RequestType.UPDATE, captor.allValues[1].requestType)
        Assert.assertEquals(RequestType.DELETE, captor.allValues[2].requestType)
        Assert.assertEquals(RequestType.DELETE, captor.allValues[3].requestType)
        Assert.assertEquals(RequestType.CREATE, captor.allValues[4].requestType)
        Assert.assertEquals(RequestType.CREATE, captor.allValues[5].requestType)
        verify(requestQueueMock, times(cachedRequest.size + 1)).remove(removeCaptor.capture())
        Assert.assertEquals(captor.allValues[0].requestType, removeCaptor.allValues[0].requestType)
        Assert.assertEquals(captor.allValues[1].requestType, removeCaptor.allValues[1].requestType)
        Assert.assertEquals(captor.allValues[2].requestType, removeCaptor.allValues[2].requestType)
        Assert.assertEquals(captor.allValues[3].requestType, removeCaptor.allValues[3].requestType)
        Assert.assertEquals(captor.allValues[4].requestType, removeCaptor.allValues[4].requestType)
        Assert.assertEquals(captor.allValues[5].requestType, removeCaptor.allValues[5].requestType)
    }

    @Test
    fun `when update() subscribed and request is possible with empty cache then call performUpdate()`() {
        whenever(requestQueueMock.isEmpty()).thenReturn(true)

        val testSubscriber = cacheDataSource.update(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(requestQueueMock, never()).add(any())
        verify(requestQueueMock).isEmpty()
        verify(dataSource).update(captor.capture())
        Assert.assertEquals(RequestType.UPDATE, captor.firstValue.requestType)
        Assert.assertEquals(testDataEntity, captor.firstValue.entity)
        Assert.assertNull(captor.firstValue.query)
    }

    @Test
    fun `when delete() subscribed and request is possible with empty cache then call performDelete()`() {
        whenever(requestQueueMock.isEmpty()).thenReturn(true)

        val testSubscriber = cacheDataSource.delete(query).test()

        testSubscriber.assertComplete()
        verify(requestQueueMock, never()).add(any())
        verify(requestQueueMock).isEmpty()
        verify(dataSource).delete(captor.capture())
        Assert.assertEquals(RequestType.DELETE, captor.firstValue.requestType)
        Assert.assertEquals(query, captor.firstValue.query)
        Assert.assertNull(captor.firstValue.entity)
    }
}
