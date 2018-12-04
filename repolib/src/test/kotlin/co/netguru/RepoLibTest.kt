package co.netguru

import co.netguru.data.Query
import co.netguru.data.TargetType
import co.netguru.datasource.DataSourceController
import co.netguru.strategy.Strategy
import co.netguru.strategy.StrategyType
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
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

    private val localDataSource: DataSourceController<String> = mock {
        on { dataOutput() } doReturn Flowable.fromIterable(localData)
        on { fetch(any()) } doReturn Completable.complete()
        on { delete(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    private val remoteDataSource: DataSourceController<String> = mock {
        on { dataOutput() } doReturn Flowable.fromIterable(remoteData)
        on { fetch(any()) } doReturn Completable.complete()
        on { create(any()) } doReturn Completable.complete()
        on { delete(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    private val testEntity = "test"
    private val strategyMock: Strategy = mock()
    private val query: Query<String> = mock()

    private val repoLib = RepoLib(localDataSource, remoteDataSource, strategyMock)


    @Test
    fun `when only LOCAL fetching strategy is selected, then call fetch on LOCAL data source only`() {
        whenever(strategyMock.fetchingStrategy()).thenReturn(StrategyType.FetchingStrategy.OnlyLocal)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(query)
        verify(remoteDataSource, never()).fetch(any())
        verify(strategyMock).fetchingStrategy()
    }

    @Test
    fun `when only REMOTE fetching strategy is selected,  then call fetch on REMOTE data source only`() {
        whenever(strategyMock.fetchingStrategy()).thenReturn(StrategyType.FetchingStrategy.OnlyRemote)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource, never()).fetch(query)
        verify(remoteDataSource).fetch(any())
        verify(strategyMock).fetchingStrategy()
    }

    @Test
    fun `when BOTH fetching strategy is selected, then call fetch on both data sources with same query object`() {
        whenever(strategyMock.fetchingStrategy()).thenReturn(StrategyType.FetchingStrategy.Both)

        val subscriber = repoLib.fetch(query).test()

        subscriber.assertComplete()
        verify(localDataSource).fetch(query)
        verify(remoteDataSource).fetch(query)
        verify(strategyMock).fetchingStrategy()
    }

    @Test
    fun `when output strategy is LOCAL then publish data from LOCAL data source only`() {
        whenever(strategyMock.outputStrategy()).thenReturn(StrategyType.SourceStrategy.Local)

        val subscriber = repoLib.outputDataStream().test()

        subscriber.assertValueCount(localData.size)
        subscriber.assertValues(localData[0], localData[1], localData[2])
        verify(localDataSource).dataOutput()
        verify(remoteDataSource, never()).dataOutput()
        verify(strategyMock).outputStrategy()
    }

    @Test
    fun `when output strategy is REMOTE then publish data from REMOTE data source only`() {
        whenever(strategyMock.outputStrategy()).thenReturn(StrategyType.SourceStrategy.Remote)

        val subscriber = repoLib.outputDataStream().test()

        subscriber.assertValueCount(remoteData.size)
        subscriber.assertValues(remoteData[0], remoteData[1], remoteData[2], remoteData[3])
        verify(localDataSource, never()).dataOutput()
        verify(remoteDataSource).dataOutput()
        verify(strategyMock).outputStrategy()
    }

    @Test
    fun `when output strategy is MERGE then publish data from both data sources`() {
        whenever(strategyMock.outputStrategy()).thenReturn(StrategyType.SourceStrategy.Merge)

        val subscriber = repoLib.outputDataStream().test()

        subscriber.assertValueCount(localData.size + remoteData.size)
        subscriber.assertValues(
                localData[0],
                localData[1],
                localData[2],
                remoteData[0],
                remoteData[1],
                remoteData[2],
                remoteData[3]
        )
        verify(strategyMock).outputStrategy()
        verify(localDataSource).dataOutput()
        verify(remoteDataSource).dataOutput()
    }

    @Test
    fun `when output strategy is EmitSecondaryUpdatedWithPrimary, then get remote, update and emit local `() {
        whenever(strategyMock.outputStrategy())
                .thenReturn(StrategyType.SourceStrategy.EmitLocalUpdatedByPrimary)

        val subscriber = repoLib.outputDataStream().test()

        subscriber.assertValueCount(localData.size)
        subscriber.assertValues(
                localData[0],
                localData[1],
                localData[2]
        )
        verify(strategyMock).outputStrategy()
        verify(remoteDataSource).dataOutput()
        verify(localDataSource, times(remoteData.size)).update(any())
        verify(localDataSource).dataOutput()
    }

    @Test
    fun `when output strategy is EmitSecondaryOnPrimaryFailure and remote return Exception, then emit local `() {
        whenever(strategyMock.outputStrategy())
                .thenReturn(StrategyType.SourceStrategy.EmitLocalOnRemoteFailure)
        whenever(remoteDataSource.dataOutput()).thenReturn(Flowable.error(Throwable()))

        val subscriber = repoLib.outputDataStream().test()

        subscriber.assertValueCount(localData.size)
        subscriber.assertValues(
                localData[0],
                localData[1],
                localData[2]
        )
        verify(strategyMock).outputStrategy()
        verify(remoteDataSource).dataOutput()
        verify(localDataSource, never()).update(any())
        verify(localDataSource).dataOutput()
    }

    @Test
    fun `when CREATE method is called then call CREATE on remote data source with same entity object`() {

        val subscriber = repoLib.create(testEntity).test()

        subscriber.assertComplete()
        verify(remoteDataSource).create(testEntity)
        verify(localDataSource, never()).create(any())
    }

    @Test
    fun `when DELETE method is called with Query targeted to remote then call DELETE on remote data source with same entity object`() {
        val remoteQuery = Query<String>(TargetType.REMOTE)

        val subscriber = repoLib.delete(remoteQuery).test()

        subscriber.assertComplete()
        verify(remoteDataSource).delete(remoteQuery)
        verify(localDataSource, never()).delete(any())
    }

    @Test
    fun `when DELETE method is called with Query targeted to LOCAL then call DELETE on remote data source with same entity object`() {
        val localQuery = Query<String>(TargetType.LOCAL)

        val subscriber = repoLib.delete(localQuery).test()

        subscriber.assertComplete()
        verify(remoteDataSource, never()).delete(any())
        verify(localDataSource).delete(localQuery)
    }

    @Test
    fun `when UPDATE method is called then call UPDATE on remote data source with same entity object`() {

        val subscriber = repoLib.update(testEntity).test()

        subscriber.assertComplete()
        verify(remoteDataSource).update(testEntity)
        verify(localDataSource, never()).create(any())
    }

    @Test
    fun `when DELETE method is called with Query targeted to BORH then call DELETE on  both sources`() {
        val localQuery = Query<String>(TargetType.BOTH)

        val subscriber = repoLib.delete(localQuery).test()

        subscriber.assertComplete()
        verify(remoteDataSource).delete(localQuery)
        verify(localDataSource).delete(localQuery)
    }
}