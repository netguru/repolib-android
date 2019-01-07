package co.netguru.repolibrx.integration

import co.netguru.repolibrx.TestDataEntity
import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.initializer.createRepo
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Test are created for whole dependency stack. Only Data Sources are mocked.
 * Also, to test behavior of the library, DefaultRequestsStrategyFactory strategy is used.
 * DefaultStrategy uses RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote for all FETCH requests.
 * All other types of requests will be sent directly to the Remote data source by default
 * using DefaultRequestsStrategyFactory logic.
 */
@RunWith(MockitoJUnitRunner::class)
class IntegrationTests {

    private val localData = listOf(
            TestDataEntity(11),
            TestDataEntity(22),
            TestDataEntity(33)
    )
    private val remoteData = listOf(
            TestDataEntity(99),
            TestDataEntity(88),
            TestDataEntity(77)
    )

    private val testDataEntityMock: TestDataEntity = mock()
    private val query: Query = mock()
    private val localDataSourceMock: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Observable.fromIterable(localData)
    }

    private val remoteDataSourceMock: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Observable.fromIterable(remoteData)
        on { update(any()) } doAnswer { invocationOnMock ->
            @Suppress("UNCHECKED_CAST")
            Observable.just(invocationOnMock.arguments[0] as TestDataEntity)
        }
        on { create(any()) } doAnswer { invocationOnMock ->
            @Suppress("UNCHECKED_CAST")
            Observable.just(invocationOnMock.arguments[0] as TestDataEntity)
        }
        on { delete(any()) } doReturn (Observable.just(remoteData.last()))
    }

    private val repoLib = createRepo<TestDataEntity> {
        localDataSourceController = localDataSourceMock
        remoteDataSourceController = remoteDataSourceMock
    }

    /**
     * FETCHing tests and data distribution
     */
    @Test
    fun `when only outputDataStream() is subscribed then do not publish anything`() {

        val dataSubscriber = repoLib.outputDataStream().test()

        dataSubscriber.assertNoErrors()
        dataSubscriber.assertNoValues()
        verify(remoteDataSourceMock, never()).fetch(any())
        verify(localDataSourceMock, never()).update(any())
        verify(localDataSourceMock, never()).fetch(any())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
    }

    @Test
    fun `when FETCH is subscribed and LocalData Source emit items, then receive items from LOCAL`() {
        whenever(localDataSourceMock.delete(any())).thenReturn(Completable.complete().toObservable())
        whenever(localDataSourceMock.create(any())).thenReturn(Completable.complete().toObservable())

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.fetch(query).test()

        requestSubscriber.assertComplete()
        dataSubscriber.assertNoErrors()
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
        dataSubscriber.assertValueCount(localData.size)
        verify(remoteDataSourceMock).fetch(any())
        verify(localDataSourceMock).delete(any())
        verify(localDataSourceMock, times(remoteData.size)).create(any())
        verify(localDataSourceMock).fetch(any())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
    }

    /**
     * Tests for CREATE requests
     */
    @Test
    fun `when CREATE is subscribed then send request to the Remote DataSource only`() {
        val dataCaptor = argumentCaptor<TestDataEntity>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.create(testDataEntityMock).test()

        dataSubscriber.assertValue(testDataEntityMock)
        dataSubscriber.assertNoErrors()
        requestSubscriber.assertComplete()
        requestSubscriber.assertNoValues()
        requestSubscriber.assertNoErrors()
        verify(localDataSourceMock, never()).create(any())
        verify(remoteDataSourceMock).create(dataCaptor.capture())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
        Assert.assertEquals(testDataEntityMock, dataCaptor.firstValue)
    }

    @Test
    fun `when CREATE is subscribed and request fails then return error`() {
        val error = Throwable("test")
        whenever(remoteDataSourceMock.create(any())).thenReturn(Observable.error(error))
        val dataCaptor = argumentCaptor<TestDataEntity>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.create(testDataEntityMock).test()

        dataSubscriber.assertNoValues()
        dataSubscriber.assertNoErrors()
        requestSubscriber.assertNoValues()
        requestSubscriber.assertError(error)
        verify(localDataSourceMock, never()).create(any())
        verify(remoteDataSourceMock).create(dataCaptor.capture())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
        Assert.assertEquals(testDataEntityMock, dataCaptor.firstValue)
    }

    /**
     * Tests for UPDATE requests
     */
    @Test
    fun `when UPDATE is subscribed then send request to the Remote DataSource only`() {
        val dataCaptor = argumentCaptor<TestDataEntity>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.update(testDataEntityMock).test()

        dataSubscriber.assertValue(testDataEntityMock)
        dataSubscriber.assertNoErrors()
        requestSubscriber.assertComplete()
        requestSubscriber.assertNoValues()
        requestSubscriber.assertNoErrors()
        verify(localDataSourceMock, never()).update(any())
        verify(remoteDataSourceMock).update(dataCaptor.capture())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
        Assert.assertEquals(testDataEntityMock, dataCaptor.firstValue)
    }

    @Test
    fun `when UPDATE is subscribed and request fails then return error`() {
        val error = Throwable("test")
        whenever(remoteDataSourceMock.update(any())).thenReturn(Observable.error(error))
        val dataCaptor = argumentCaptor<TestDataEntity>()


        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.update(testDataEntityMock).test()

        dataSubscriber.assertNoValues()
        dataSubscriber.assertNoErrors()
        requestSubscriber.assertNoValues()
        requestSubscriber.assertError(error)
        verify(localDataSourceMock, never()).update(any())
        verify(remoteDataSourceMock).update(dataCaptor.capture())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
        Assert.assertEquals(testDataEntityMock, dataCaptor.firstValue)
    }

    /**
     * Tests for DELETE requests
     */
    @Test
    fun `when DELETE is subscribed then send request to the Remote DataSource only`() {
        val queryCaptor = argumentCaptor<Query>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.delete(query).test()

        dataSubscriber.assertNoErrors()
        requestSubscriber.assertComplete()
        requestSubscriber.assertNoValues()
        requestSubscriber.assertNoErrors()
        verify(localDataSourceMock, never()).delete(any())
        verify(remoteDataSourceMock).delete(queryCaptor.capture())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
        Assert.assertEquals(query, queryCaptor.firstValue)
    }

    @Test
    fun `when DELETE is subscribed and request fails then return error`() {
        val error = Throwable("test")
        whenever(remoteDataSourceMock.delete(any())).thenReturn(Observable.error(error))
        val queryCaptor = argumentCaptor<Query>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.delete(query).test()

        dataSubscriber.assertNoValues()
        dataSubscriber.assertNoErrors()
        requestSubscriber.assertNoValues()
        requestSubscriber.assertError(error)
        verify(localDataSourceMock, never()).delete(any())
        verify(remoteDataSourceMock).delete(queryCaptor.capture())
        verifyNoMoreInteractions(localDataSourceMock)
        verifyNoMoreInteractions(remoteDataSourceMock)
        Assert.assertEquals(query, queryCaptor.firstValue)
    }

    /**
     * Tests for multiple request with Queue
     */
    @Test
    fun `when four requests send then send it to remote data source`() {

        val dataSubscriber = repoLib.outputDataStream().test()
        val createSubscriber = repoLib.create(testDataEntityMock).test()
        val updateSubscriber = repoLib.update(testDataEntityMock).test()
        val deleteSubscriber = repoLib.delete(query).test()
        val delete2Subscriber = repoLib.delete(query).test()

        createSubscriber.assertNoErrors()
        createSubscriber.assertComplete()
        createSubscriber.assertNoValues()
        updateSubscriber.assertNoErrors()
        updateSubscriber.assertComplete()
        updateSubscriber.assertNoValues()
        deleteSubscriber.assertNoErrors()
        deleteSubscriber.assertComplete()
        deleteSubscriber.assertNoValues()
        dataSubscriber.assertValues(testDataEntityMock, testDataEntityMock, remoteData.last(), remoteData.last())
        dataSubscriber.assertValueCount(4)
        dataSubscriber.assertNoErrors()
        delete2Subscriber.assertNoErrors()
        delete2Subscriber.assertComplete()
        delete2Subscriber.assertNoValues()
        verify(remoteDataSourceMock).create(any())
        verify(remoteDataSourceMock).update(any())
        verify(remoteDataSourceMock, times(2)).delete(any())
        verifyNoMoreInteractions(remoteDataSourceMock)
        verifyNoMoreInteractions(localDataSourceMock)
    }

    @Test
    fun `when two requests fails then fail all subscribers expect data`() {
        val error = Throwable("")
        whenever(remoteDataSourceMock.create(any())).thenReturn(Observable.error(error))
        whenever(remoteDataSourceMock.update(any())).thenReturn(Observable.error(error))

        val dataSubscriber = repoLib.outputDataStream().test()
        val createSubscriberFailed = repoLib.create(testDataEntityMock).test()
        val updateSubscriberFailed = repoLib.update(testDataEntityMock).test()

        createSubscriberFailed.assertError(error)
        createSubscriberFailed.assertNoValues()
        updateSubscriberFailed.assertError(Throwable::class.java)
        updateSubscriberFailed.assertNoValues()
        dataSubscriber.assertNoErrors()
        dataSubscriber.assertNoValues()
        //two times because first request will fail
        verify(remoteDataSourceMock).create(any())
        verify(remoteDataSourceMock).update(any())
        verifyNoMoreInteractions(remoteDataSourceMock)
        verifyNoMoreInteractions(localDataSourceMock)
    }

    @Test
    fun `when all requests fails and one succeed then return data only for one`() {
        val error = Throwable("")
        whenever(remoteDataSourceMock.delete(any())).thenReturn(Observable.error(error))
                .thenReturn(Observable.just(remoteData.last()))
        whenever(remoteDataSourceMock.update(any())).thenReturn(Observable.error(error))
                .thenReturn(Observable.just(remoteData.last()))
        whenever(remoteDataSourceMock.create(any()))
                .thenReturn(Observable.just(remoteData.last()))

        val dataSubscriber = repoLib.outputDataStream().test()
        val updateSubscriberFailed = repoLib.update(testDataEntityMock).test()
        val deleteSubscriberFailed = repoLib.delete(query).test()
        val createSubscriberSuccess = repoLib.create(testDataEntityMock).test()

        deleteSubscriberFailed.assertError(error)
        deleteSubscriberFailed.assertNoValues()
        updateSubscriberFailed.assertError(error)
        updateSubscriberFailed.assertNoValues()
        createSubscriberSuccess.assertNoErrors()
        createSubscriberSuccess.assertComplete()
        createSubscriberSuccess.assertNoValues()
        dataSubscriber.assertNoErrors()
        dataSubscriber.assertValueCount(1)
        dataSubscriber.assertValue(remoteData.last())
        verify(remoteDataSourceMock).delete(any())
        verify(remoteDataSourceMock).update(any())
        verify(remoteDataSourceMock).create(any())
        verifyNoMoreInteractions(remoteDataSourceMock)
        verifyNoMoreInteractions(localDataSourceMock)
    }

    @Test
    fun `when three requests fails and FETCH succeed then return update local and return only fetch data`() {
        val error = Throwable("")
        whenever(remoteDataSourceMock.delete(any())).thenReturn(Observable.error(error))
        whenever(localDataSourceMock.delete(any())).thenReturn(Completable.complete().toObservable())
        whenever(remoteDataSourceMock.update(any())).thenReturn(Observable.error(error))
        whenever(remoteDataSourceMock.create(any())).thenReturn(Observable.error(error))
        whenever(localDataSourceMock.create(any())).thenReturn(Completable.complete().toObservable())

        val dataSubscriber = repoLib.outputDataStream().test()
        val updateSubscriberFailed = repoLib.update(testDataEntityMock).test()
        val deleteSubscriberFailed = repoLib.delete(query).test()
        val createSubscriberFailed = repoLib.create(testDataEntityMock).test()
        val fetchSubscriberSuccess = repoLib.fetch(query).test()

        deleteSubscriberFailed.assertError(error)
        deleteSubscriberFailed.assertNoValues()
        updateSubscriberFailed.assertError(error)
        updateSubscriberFailed.assertNoValues()
        createSubscriberFailed.assertNoValues()
        createSubscriberFailed.assertError(error)
        dataSubscriber.assertNoErrors()
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
        dataSubscriber.assertValueCount(localData.size)
        fetchSubscriberSuccess.assertComplete()
        fetchSubscriberSuccess.assertNoValues()
        fetchSubscriberSuccess.assertNoErrors()
        verify(remoteDataSourceMock).delete(any())
        verify(remoteDataSourceMock).update(any())
        verify(remoteDataSourceMock).create(any())
        verify(remoteDataSourceMock).fetch(any())
        verify(localDataSourceMock).delete(any())
        verify(localDataSourceMock, times(remoteData.size)).create(any())
        verify(localDataSourceMock).fetch(any())
        verifyNoMoreInteractions(remoteDataSourceMock)
        verifyNoMoreInteractions(localDataSourceMock)
    }
}