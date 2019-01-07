package co.netguru.repolibrx

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.data.Request
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.strategy.RequestStrategy
import co.netguru.repolibrx.strategy.RequestsStrategyFactory
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
    private val requestsStrategyFactoryMock: RequestsStrategyFactory = mock()
    private val query: Query = mock()
    private val requestCaptor = argumentCaptor<Request.Fetch<String>>()

    private val repoLib: RepoLibRx<String> = RepoLib(localDataSource, remoteDataSource, requestsStrategyFactoryMock)

    //Fetch tests
    @Test
    fun `when only OnlyLocal fetching strategy is selected, then call fetch on LOCAL data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)
        val requestCaptor = argumentCaptor<Request.Fetch<TestDataEntity>>()
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(queryCaptor.capture())
        verify(remoteDataSource, never()).fetch(any())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(queryCaptor.firstValue, requestCaptor.firstValue.query)
    }

    @Test
    fun `when only OnlyRemote fetching strategy is selected, then call fetch on REMOTE data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).fetch(any())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(queryCaptor.firstValue, requestCaptor.firstValue.query)
    }

    @Test
    fun `when LocalOnRemoteFailure strategy is selected, then FETCH from remote`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalOnRemoteFailure)
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).fetch(any())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        dataSubscriber.assertValueCount(remoteData.size)
        dataSubscriber.assertValues(remoteData[0], remoteData[1], remoteData[2], remoteData[3])
    }

    @Test
    fun `when LocalOnRemoteFailure strategy is selected and remote FETCH fail, then FETCH from local`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalOnRemoteFailure)
        whenever(remoteDataSource.fetch(any())).thenReturn(Observable.error(Throwable(testResponse)))
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(queryCaptor.capture())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, queryCaptor.firstValue)
        Assert.assertEquals(query, queryCaptor.secondValue)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    @Test
    fun `when LocalOnRemoteFailure strategy is selected and remote FETCH fail, then return Error`() {
        val error = Throwable(testResponse)
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalAfterFullUpdateWithRemote)
        whenever(remoteDataSource.fetch(any())).thenReturn(Observable.error(error))
        val queryCaptor = argumentCaptor<QueryWithParams>()
        val requestCaptor = argumentCaptor<Request.Fetch<TestDataEntity>>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertError(error)
        verify(localDataSource, never()).fetch(any())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(query, queryCaptor.firstValue)
        dataSubscriber.assertNoValues()
    }

    @Test
    fun `when LocalAfterFullUpdateOrFailureOfRemote strategy is selected, then FETCH from remote update local and FETCH from local`() {
        val dataCaptor = argumentCaptor<String>()
        val requestCaptor = argumentCaptor<Request.Fetch<String>>()
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>()))
                .thenReturn(RequestStrategy.LocalAfterFullUpdateWithRemote)
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(queryCaptor.capture())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        verify(localDataSource).delete(any())
        verify(localDataSource, times(remoteData.size)).create(dataCaptor.capture())
        Assert.assertEquals(dataCaptor.allValues[0], remoteData[0])
        Assert.assertEquals(dataCaptor.allValues[1], remoteData[1])
        Assert.assertEquals(dataCaptor.allValues[2], remoteData[2])
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    @Test
    fun `when LocalAfterFullUpdateOrFailureOfRemote strategy is selected, then FETCH from remote UPDATE local and FETCH local`() {
        val dataCaptor = argumentCaptor<String>()
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>()))
                .thenReturn(RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote)
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(queryCaptor.capture())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        verify(localDataSource).delete(any())
        verify(localDataSource, times(remoteData.size)).create(dataCaptor.capture())
        Assert.assertEquals(dataCaptor.allValues[0], remoteData[0])
        Assert.assertEquals(dataCaptor.allValues[1], remoteData[1])
        Assert.assertEquals(dataCaptor.allValues[2], remoteData[2])
        Assert.assertEquals(query, queryCaptor.firstValue)
        Assert.assertEquals(query, queryCaptor.secondValue)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    @Test
    fun `when LocalAfterUpdateOrFailureOfRemote strategy is selected and remote FETCH fail, then FETCh from local`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.LocalAfterFullUpdateOrFailureOfRemote)
        whenever(remoteDataSource.fetch(any())).thenReturn(Observable.error(Throwable(testResponse)))
        val queryCaptor = argumentCaptor<QueryWithParams>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(queryCaptor.capture())
        verify(remoteDataSource).fetch(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, queryCaptor.firstValue)
        Assert.assertEquals(query, queryCaptor.secondValue)
        dataSubscriber.assertValueCount(localData.size)
        dataSubscriber.assertValues(localData[0], localData[1], localData[2])
    }

    //CREATE tests
    @Test
    fun `when only OnlyLocal strategy is selected and shouldBeDataPublished is true, then call CREATE on LOCAL data source only and publish data`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)
        val dataCaptor = argumentCaptor<String>()
        val requestCaptor = argumentCaptor<Request.Create<TestDataEntity>>()

        val dataSubscriber = repoLib.outputDataStream().test()
        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        dataSubscriber.assertValues(testResponse)
        verify(localDataSource).create(dataCaptor.capture())
        verify(remoteDataSource, never()).create(any())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
        Assert.assertEquals(requestCaptor.firstValue.entity, dataCaptor.firstValue)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call CREATE on REMOTE data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)
        val requestCaptor = argumentCaptor<Request.Create<String>>()
        val dataCaptor = argumentCaptor<String>()

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).create(any())
        verify(remoteDataSource).create(dataCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
    }

    @Test
    fun `when BOTH strategy is selected, then call CREATE on both data sources with same query object`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.Both)
        val requestCaptor = argumentCaptor<Request.Create<String>>()
        val dataCaptor = argumentCaptor<String>()

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).create(dataCaptor.capture())
        verify(remoteDataSource).create(dataCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
        Assert.assertEquals(testEntity, dataCaptor.lastValue)
    }

    //UPDATE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call UPDATE on LOCAL data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)
        val dataCaptor = argumentCaptor<String>()

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).update(dataCaptor.capture())
        verify(remoteDataSource, never()).update(any())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call UPDATE on REMOTE data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)
        val requestCaptor = argumentCaptor<Request.Update<String>>()
        val dataCaptor = argumentCaptor<String>()

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).update(any())
        verify(remoteDataSource).update(dataCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
    }

    @Test
    fun `when BOTH strategy is selected, then call UPATE on both data sources with same query object`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.Both)
        val requestCaptor = argumentCaptor<Request.Update<String>>()
        val dataCaptor = argumentCaptor<String>()

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).update(dataCaptor.capture())
        verify(remoteDataSource).update(dataCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
        Assert.assertEquals(testEntity, dataCaptor.firstValue)
    }

    //DELETE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call DELETE on LOCAL data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyLocal)
        val requestCaptor = argumentCaptor<Request.Update<String>>()
        val queryCaptor = argumentCaptor<Query>()

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource).delete(queryCaptor.capture())
        verify(remoteDataSource, never()).delete(any())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, queryCaptor.firstValue)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call DELETE on REMOTE data source only`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.OnlyRemote)
        val requestCaptor = argumentCaptor<Request.Delete<String>>()
        val queryCaptor = argumentCaptor<Query>()

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).delete(any())
        verify(remoteDataSource).delete(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, queryCaptor.firstValue)
    }

    @Test
    fun `when BOTH strategy is selected, then call DELETE on both data sources with same query object`() {
        whenever(requestsStrategyFactoryMock.select(any<Request<String>>())).thenReturn(RequestStrategy.Both)
        val requestCaptor = argumentCaptor<Request.Delete<String>>()
        val queryCaptor = argumentCaptor<Query>()

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource).delete(queryCaptor.capture())
        verify(remoteDataSource).delete(queryCaptor.capture())
        verify(requestsStrategyFactoryMock).select(requestCaptor.capture())
        Assert.assertEquals(query, queryCaptor.firstValue)
        Assert.assertEquals(query, queryCaptor.secondValue)
    }
}