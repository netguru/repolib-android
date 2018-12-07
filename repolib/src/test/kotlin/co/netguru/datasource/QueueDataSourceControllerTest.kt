package co.netguru.datasource

import co.netguru.TestDataEntity
import co.netguru.data.Request
import co.netguru.data.RequestType
import co.netguru.queue.RequestQueue
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QueueDataSourceControllerTest {

    private val queueList: MutableList<Request<TestDataEntity>> = mutableListOf()
    private val list = mutableListOf(
            TestDataEntity(99),
            TestDataEntity(88),
            TestDataEntity(77)
    )
    private val captor = argumentCaptor<Request<TestDataEntity>>()

    private val dataSource: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(list)
        on { delete(any()) } doReturn Completable.complete()
        on { create(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    private val requestQueue: RequestQueue<TestDataEntity> = mock {
        on { isEmpty() } doReturn queueList.isEmpty()
        on { getAllRequests() } doReturn queueList
        on { add(any()) } doAnswer { invocationOnMock ->
            @Suppress("UNCHECKED_CAST")
            queueList += invocationOnMock.arguments[0] as Request<TestDataEntity>
            Unit
        }
    }

    @Spy
    private val remoteDataSourceController = QueueDataSourceController(
            dataSource,
            requestQueue
    )

    //Tests for empty Queue
    @Test
    fun `when FETCH subscribed then call it on data source`() {
        val request: Request<TestDataEntity> = mock()

        val testSubscriber = remoteDataSourceController.fetch(request).test()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValues(list[0], list[1], list[2])
        verify(dataSource).fetch(request)
    }

    @Test
    fun `when CREATE subscribed then call it on data source`() {
        val request = Request<TestDataEntity>(RequestType.CREATE)

        val testSubscriber = remoteDataSourceController.create(request).test()

        testSubscriber.assertNoErrors()
        verify(dataSource).create(request)
        verify(requestQueue).remove(request)
    }

    @Test
    fun `when DELETE subscribed then call it on data source`() {
        val request = Request<TestDataEntity>(RequestType.DELETE)

        val testSubscriber = remoteDataSourceController.delete(request).test()

        testSubscriber.assertNoErrors()
        verify(dataSource).delete(request)
        verify(requestQueue).remove(request)
    }

    @Test
    fun `when UPDATE subscribed then call it on data source`() {
        val request = Request<TestDataEntity>(RequestType.UPDATE)

        val testSubscriber = remoteDataSourceController.update(request).test()

        testSubscriber.assertNoErrors()
        verify(dataSource).update(request)
        verify(requestQueue).remove(request)
    }

    @Test
    fun `when FETCH request is sent using create method then return IllegalStateException`() {
        val request = Request<TestDataEntity>(RequestType.FETCH)

        val testSubscriber = remoteDataSourceController.create(request).test()

        testSubscriber.assertError(IllegalStateException::class.java)
        verify(dataSource, never()).fetch(any())
        verify(dataSource, never()).create(any())
        verify(dataSource, never()).delete(any())
        verify(dataSource, never()).update(any())
    }

    //Test for not empty queue
    @Test
    fun `when Completable request subscribed and queue contains several request then execute all on DataSource`() {
        val currentRequest = Request<TestDataEntity>(RequestType.CREATE)
        queueList += listOf(
                Request(RequestType.UPDATE),
                Request(RequestType.CREATE),
                Request(RequestType.UPDATE),
                Request(RequestType.DELETE),
                Request(RequestType.UPDATE),
                Request(RequestType.DELETE)
        )

        val testSubscriber = remoteDataSourceController.create(currentRequest).test()

        testSubscriber.assertNoErrors()
        testSubscriber.assertComplete()
        verify(dataSource, times(3)).update(captor.capture())
        verify(dataSource, times(2)).delete(captor.capture())
        verify(dataSource, times(2)).create(captor.capture())
        verify(dataSource, never()).fetch(any())
        verify(requestQueue, times(queueList.size)).remove(any())
    }

    @Test
    fun `when FETCH request subscribed and queue contains several request then execute all on DataSource`() {
        whenever(requestQueue.isEmpty()).thenReturn(false)
        val currentRequest = Request<TestDataEntity>(RequestType.FETCH)
        val queue = listOf(
                Request<TestDataEntity>(RequestType.UPDATE),
                Request(RequestType.CREATE),
                Request(RequestType.UPDATE),
                Request(RequestType.DELETE),
                Request(RequestType.UPDATE),
                Request(RequestType.DELETE)

        )
        whenever(requestQueue.getAllRequests()).thenReturn(queue)

        val testSubscriber = remoteDataSourceController.fetch(currentRequest).test()

        testSubscriber.assertNoErrors()
        testSubscriber.assertComplete()
        verify(dataSource, times(3)).update(captor.capture())
        verify(dataSource, times(2)).delete(captor.capture())
        verify(dataSource, times(1)).create(captor.capture())
        verify(dataSource).fetch(any())
        verify(requestQueue, times(queue.size)).remove(any())
    }

    @Test
    fun `when CREATE request fails then add it to queue`() {
        val error = Throwable("test")
        val request = Request<TestDataEntity>(RequestType.CREATE)
        whenever(dataSource.create(request)).thenReturn(Completable.error(error))

        val testSubscriber = remoteDataSourceController.create(request).test()

        testSubscriber.assertError(error)
        verify(dataSource).create(request)
        verify(requestQueue).add(request)
    }

    @Test
    fun `when DELETE request fails then add it to queue`() {
        val error = Throwable("test")
        val request = Request<TestDataEntity>(RequestType.DELETE)
        whenever(dataSource.delete(request)).thenReturn(Completable.error(error))

        val testSubscriber = remoteDataSourceController.create(request).test()

        testSubscriber.assertError(error)
        verify(dataSource).delete(request)
        verify(requestQueue).add(request)
    }

    @Test
    fun `when UPDATE request fails then add it to queue`() {
        val error = Throwable("test")
        val request = Request<TestDataEntity>(RequestType.UPDATE)
        whenever(dataSource.update(request)).thenReturn(Completable.error(error))

        val testSubscriber = remoteDataSourceController.create(request).test()

        testSubscriber.assertError(error)
        verify(dataSource).update(request)
        verify(requestQueue).add(request)
    }
}
