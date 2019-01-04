package co.netguru.repolibrx

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.data.RequestType
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.RequestStrategy
import co.netguru.repolibrx.strategy.RequestsStrategy
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class RepoLibTest {

    private val localData = listOf(
            "local 1",
            "local 2",
            "local 3"
    )
    private val remoteData = listOf(
            "remote 1",
            "remote 2",
            "remote 3",
            "remote 4"
    )

    private val testResponse = "test"

    private val localDataSource: DataSource<String> = mock {
        on { fetch(any()) } doReturn Observable.fromIterable(localData)
        on { delete(any()) } doReturn Observable.just(testResponse)
        on { update(any()) } doReturn Observable.just(testResponse)
        on { create(any()) } doReturn Observable.just(testResponse)
    }

    private val remoteDataSource: DataSource<String> = mock {
        on { fetch(any()) } doReturn Observable.fromIterable(remoteData)
        on { create(any()) } doReturn Observable.just(testResponse)
        on { delete(any()) } doReturn Observable.just(testResponse)
        on { update(any()) } doReturn Observable.just(testResponse)
    }

    private val testEntity = "test data"
    private val requestsStrategyMock: RequestsStrategy = mock()
    private val query: Query<String> = mock()
    private val requestCaptor = argumentCaptor<Request<String>>()

    private val repoLib: RepoLibRx<String> = RepoLib(localDataSource, remoteDataSource, requestsStrategyMock)

    //Fetch tests
    @Test
    fun `when only OnlyLocal fetching strategy is selected, then call fetch on LOCAL data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource, never()).fetch(any())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote fetching strategy is selected, then call fetch on REMOTE data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).fetch(any())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
    }

    @Test
    fun `when LocalOnRemoteFailure strategy is selected, then FETCH from remote`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalOnRemoteFailure)

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
        dataSubscriber.assertValueCount(remoteData.size)
        dataSubscriber.assertValues(remoteData[0], remoteData[1], remoteData[2], remoteData[3])
    }

    @Test
    fun `when LocalOnRemoteFailure strategy is selected and remote FETCH fail, then FETCH from local`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalOnRemoteFailure)
        whenever(remoteDataSource.fetch(any())).thenReturn(Observable.error(Throwable(testResponse)))

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    @Test
    fun `when LocalOnRemoteFailure strategy is selected and remote FETCH fail, then return Error`() {
        val error = Throwable(testResponse)
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalAfterFullUpdateWithRemote)
        whenever(remoteDataSource.fetch(any())).thenReturn(Observable.error(error))

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertError(error)
        verify(localDataSource, never()).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
        dataSubscriber.assertNoValues()
    }

    @Test
    fun `when LocalAfterFullUpdateOrFailureOfRemote strategy is selected, then FETCH from remote update local and FETCH from local`() {
        val dataCaptor = argumentCaptor<Request<String>>()
        whenever(requestsStrategyMock.select(any<Request<String>>()))
                .thenReturn(RequestStrategy.LocalAfterFullUpdateWithRemote)

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        verify(localDataSource).delete(any())
        verify(localDataSource, times(remoteData.size)).create(dataCaptor.capture())
        Assert.assertEquals(dataCaptor.allValues[0].entity, remoteData[0])
        Assert.assertEquals(dataCaptor.allValues[1].entity, remoteData[1])
        Assert.assertEquals(dataCaptor.allValues[2].entity, remoteData[2])
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    @Test
    fun `when LocalAfterFullUpdateOrFailureOfRemote strategy is selected, then FETCH from remote UPDATE local and FETCH local`() {
        val dataCaptor = argumentCaptor<Request<String>>()
        whenever(requestsStrategyMock.select(any<Request<String>>()))
                .thenReturn(RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote)

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        verify(localDataSource).delete(any())
        verify(localDataSource, times(remoteData.size)).create(dataCaptor.capture())
        Assert.assertEquals(dataCaptor.allValues[0].entity, remoteData[0])
        Assert.assertEquals(dataCaptor.allValues[1].entity, remoteData[1])
        Assert.assertEquals(dataCaptor.allValues[2].entity, remoteData[2])
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    @Test
    fun `when LocalAfterUpdateOrFailureOfRemote strategy is selected and remote FETCH fail, then FETCh from local`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote)
        whenever(remoteDataSource.fetch(any())).thenReturn(Observable.error(Throwable(testResponse)))

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    //CREATE tests
    @Test
    fun `when only OnlyLocal strategy is selected and shouldBeDataPublished is true, then call CREATE on LOCAL data source only and publish data`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        dataSubscriber.assertValues(testResponse)
        verify(localDataSource).create(requestCaptor.capture())
        verify(remoteDataSource, never()).create(any())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(requestCaptor.firstValue, requestCaptor.secondValue)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call CREATE on REMOTE data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).create(any())
        verify(remoteDataSource).create(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when BOTH strategy is selected, then call CREATE on both data sources with same query object`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.Both)

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).create(requestCaptor.capture())
        verify(remoteDataSource).create(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.secondValue.type)
    }

    //UPDATE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call UPDATE on LOCAL data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).update(requestCaptor.capture())
        verify(remoteDataSource, never()).update(any())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call UPDATE on REMOTE data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).update(any())
        verify(remoteDataSource).update(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when BOTH strategy is selected, then call UPATE on both data sources with same query object`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.Both)

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).update(requestCaptor.capture())
        verify(remoteDataSource).update(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.secondValue.type)
    }

    //DELETE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call DELETE on LOCAL data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource).delete(requestCaptor.capture())
        verify(remoteDataSource, never()).delete(any())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call DELETE on REMOTE data source only`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).delete(any())
        verify(remoteDataSource).delete(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when BOTH strategy is selected, then call DELETE on both data sources with same query object`() {
        whenever(requestsStrategyMock.select(any<Request<String>>())).thenReturn(RequestStrategy.Both)

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource).delete(requestCaptor.capture())
        verify(remoteDataSource).delete(requestCaptor.capture())
        verify(requestsStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.secondValue.type)
    }
}