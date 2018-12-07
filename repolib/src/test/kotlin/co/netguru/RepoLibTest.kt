package co.netguru

import co.netguru.data.Query
import co.netguru.data.Request
import co.netguru.data.RequestType
import co.netguru.datasource.DataSource
import co.netguru.strategy.SourcingStrategy
import co.netguru.strategy.StrategyType
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
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

    private val localDataSource: DataSource<String> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(localData)
        on { delete(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
        on { create(any()) } doReturn Completable.complete()
    }

    private val remoteDataSource: DataSource<String> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(remoteData)
        on { create(any()) } doReturn Completable.complete()
        on { delete(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    private val testEntity = "test data"
    private val sourcingStrategyMock: SourcingStrategy = mock()
    private val query: Query<String> = mock()
    private val requestCaptor = argumentCaptor<Request<String>>()

    private val repoLib = RepoLib(localDataSource, remoteDataSource, sourcingStrategyMock)

    //Fetch tests
    @Test
    fun `when only OnlyLocal fetching strategy is selected, then call fetch on LOCAL data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyLocal)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource, never()).fetch(any())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote fetching strategy is selected, then call fetch on REMOTE data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyRemote)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).fetch(any())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
    }

    @Test
    fun `when RemoteOnFailureLocal strategy is selected, then call FETCH on both data sources with same query object`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.RemoteOnFailureLocal)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(requestCaptor.capture())
        verify(remoteDataSource).fetch(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.FETCH, requestCaptor.secondValue.type)
    }

    //CREATE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call CREATE on LOCAL data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyLocal)

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).create(requestCaptor.capture())
        verify(remoteDataSource, never()).create(any())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call CREATE on REMOTE data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyRemote)

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).create(any())
        verify(remoteDataSource).create(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when RemoteOnFailureLocal strategy is selected, then call CREATE on both data sources with same query object`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.RemoteOnFailureLocal)

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).create(requestCaptor.capture())
        verify(remoteDataSource).create(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.CREATE, requestCaptor.secondValue.type)
    }

    //UPDATE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call UPDATE on LOCAL data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyLocal)

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).update(requestCaptor.capture())
        verify(remoteDataSource, never()).update(any())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call UPDATE on REMOTE data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyRemote)

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).update(any())
        verify(remoteDataSource).update(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when RemoteOnFailureLocal strategy is selected, then call UPATE on both data sources with same query object`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.RemoteOnFailureLocal)

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(localDataSource).update(requestCaptor.capture())
        verify(remoteDataSource).update(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(testEntity, requestCaptor.firstValue.entity)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.UPDATE, requestCaptor.secondValue.type)
    }

    //DELETE tests
    @Test
    fun `when only OnlyLocal strategy is selected, then call DELETE on LOCAL data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyLocal)

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource).delete(requestCaptor.capture())
        verify(remoteDataSource, never()).delete(any())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when only OnlyRemote strategy is selected, then call DELETE on REMOTE data source only`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.OnlyRemote)

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).delete(any())
        verify(remoteDataSource).delete(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.secondValue.type)
    }

    @Test
    fun `when RemoteOnFailureLocal strategy is selected, then call DELETE on both data sources with same query object`() {
        whenever(sourcingStrategyMock.select(any<Request<String>>())).thenReturn(StrategyType.Requests.RemoteOnFailureLocal)

        val subscriber = repoLib.delete(query).test()

        subscriber.assertComplete()
        verify(localDataSource).delete(requestCaptor.capture())
        verify(remoteDataSource).delete(requestCaptor.capture())
        verify(sourcingStrategyMock).select(requestCaptor.capture())
        Assert.assertEquals(query, requestCaptor.firstValue.query)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.firstValue.type)
        Assert.assertEquals(RequestType.DELETE, requestCaptor.secondValue.type)
    }
}