package co.netguru.datasource

import co.netguru.TestDataEntity
import co.netguru.cache.Cache
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
class CachedDataSourceControllerTest {


    private val testDataEntity = TestDataEntity(99)
    private val query: Query<TestDataEntity> = mock()
    private val captor = argumentCaptor<Request<TestDataEntity>>()
    private val list = mutableListOf(testDataEntity)

    private val cacheMock: Cache<TestDataEntity> = mock {}
    private val cachingValidator: CachingValidator = mock {}
    private val dataSource: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(list)
        on { delete(any()) } doReturn Completable.complete()
        on { create(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    @Spy
    private val cacheDataSource = CachedDataSourceController(cacheMock, dataSource, cachingValidator)

    @Test
    fun `when fetch() is subscribed then call fetch() and return data in output`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)

        val testSubscriber = cacheDataSource.fetch(query).test()
        val dataSubscriber = cacheDataSource.dataOutput().test()

        testSubscriber.assertComplete()
        dataSubscriber.assertValues(testDataEntity)
        verify(cachingValidator).isOperationPermitted()
        verify(dataSource).fetch(captor.capture())
        verify(cachingValidator).isOperationPermitted()
        val result = captor.firstValue
        Assert.assertEquals(query, result.query)
        verify(cacheMock, never()).add(any())
        Assert.assertEquals(RequestType.FETCH, result.requestType)
        verify(cacheMock).remove(result)
    }

    @Test
    fun `when fetch() is subscribed and throw exception then call query() and return exception on both streams and add request to cache`() {
        val error = Throwable("test")
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)
        whenever(cacheMock.isEmpty()).thenReturn(true)
        whenever(dataSource.fetch(any())).thenReturn(Flowable.error(error))

        val testSubscriber = cacheDataSource.fetch(query).test()
        val dataSubscriber = cacheDataSource.dataOutput().test()

        testSubscriber.assertError(error)
        dataSubscriber.assertError(error)
        verify(cachingValidator).isOperationPermitted()
        verify(dataSource).fetch(captor.capture())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock).add(any())
        val result = captor.firstValue
        Assert.assertEquals(query, result.query)
        Assert.assertEquals(RequestType.FETCH, result.requestType)
        verify(cacheMock, never()).remove(result)
    }

    @Test
    fun `when create() subscribed and request is possible with empty cache then call create()`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)
        whenever(cacheMock.isEmpty()).thenReturn(true)

        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(cacheMock, never()).add(any())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock).isEmpty()
        verify(dataSource).create(captor.capture())
        Assert.assertEquals(RequestType.CREATE, captor.firstValue.requestType)
        Assert.assertEquals(testDataEntity, captor.firstValue.entity)
        Assert.assertNull(captor.firstValue.query)
    }

    @Test
    fun `when create() subscribed and request is NOT possible then call addToCache`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(false)

        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(cacheMock).add(captor.capture())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock, never()).isEmpty()
        verify(dataSource, never()).fetch(any())
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
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)
        whenever(cacheMock.isEmpty()).thenReturn(false)
        whenever(cacheMock.getAllRequests()).thenReturn(cachedRequest)
        val removeCaptor = argumentCaptor<Request<TestDataEntity>>()

        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(cacheMock).isEmpty()
        verify(cacheMock).getAllRequests()
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
        verify(cacheMock, times(cachedRequest.size + 1)).remove(removeCaptor.capture())
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
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)
        whenever(cacheMock.isEmpty()).thenReturn(false)
        whenever(cacheMock.getAllRequests()).thenReturn(cachedRequest)
        val removeCaptor = argumentCaptor<Request<TestDataEntity>>()

        val dataSubscriber = cacheDataSource.dataOutput().test()
        val testSubscriber = cacheDataSource.create(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(cacheMock).isEmpty()
        verify(cacheMock).getAllRequests()
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
        verify(cacheMock, times(cachedRequest.size + 1)).remove(removeCaptor.capture())
        Assert.assertEquals(captor.allValues[0].requestType, removeCaptor.allValues[0].requestType)
        Assert.assertEquals(captor.allValues[1].requestType, removeCaptor.allValues[1].requestType)
        Assert.assertEquals(captor.allValues[2].requestType, removeCaptor.allValues[2].requestType)
        Assert.assertEquals(captor.allValues[3].requestType, removeCaptor.allValues[3].requestType)
        Assert.assertEquals(captor.allValues[4].requestType, removeCaptor.allValues[4].requestType)
        Assert.assertEquals(captor.allValues[5].requestType, removeCaptor.allValues[5].requestType)
    }

    @Test
    fun `when update() subscribed and request is possible with empty cache then call performUpdate()`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)
        whenever(cacheMock.isEmpty()).thenReturn(true)

        val testSubscriber = cacheDataSource.update(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(cacheMock, never()).add(any())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock).isEmpty()
        verify(dataSource).update(captor.capture())
        Assert.assertEquals(RequestType.UPDATE, captor.firstValue.requestType)
        Assert.assertEquals(testDataEntity, captor.firstValue.entity)
        Assert.assertNull(captor.firstValue.query)
    }

    @Test
    fun `when update() subscribed and request is NOT possible then call addToCache`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(false)

        val testSubscriber = cacheDataSource.update(testDataEntity).test()

        testSubscriber.assertComplete()
        verify(cacheMock).add(captor.capture())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock, never()).isEmpty()
        verify(dataSource, never()).update(any())
        Assert.assertEquals(RequestType.UPDATE, captor.firstValue.requestType)
        Assert.assertEquals(testDataEntity, captor.firstValue.entity)
        Assert.assertNull(captor.firstValue.query)
    }

    @Test
    fun `when delete() subscribed and request is possible with empty cache then call performDelete()`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(true)
        whenever(cacheMock.isEmpty()).thenReturn(true)

        val testSubscriber = cacheDataSource.delete(query).test()

        testSubscriber.assertComplete()
        verify(cacheMock, never()).add(any())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock).isEmpty()
        verify(dataSource).delete(captor.capture())
        Assert.assertEquals(RequestType.DELETE, captor.firstValue.requestType)
        Assert.assertEquals(query, captor.firstValue.query)
        Assert.assertNull(captor.firstValue.entity)
    }

    @Test
    fun `when delete() subscribed and request is NOT possible then call addToCache`() {
        whenever(cachingValidator.isOperationPermitted()).thenReturn(false)

        val testSubscriber = cacheDataSource.delete(query).test()

        testSubscriber.assertComplete()
        verify(cacheMock).add(captor.capture())
        verify(cachingValidator).isOperationPermitted()
        verify(cacheMock, never()).isEmpty()
        verify(dataSource, never()).delete(any())
        Assert.assertEquals(RequestType.DELETE, captor.firstValue.requestType)
        Assert.assertEquals(query, captor.firstValue.query)
        Assert.assertNull(captor.firstValue.entity)
    }
}
