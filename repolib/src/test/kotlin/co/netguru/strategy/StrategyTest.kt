package co.netguru.strategy

import co.netguru.datasource.DataSource
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StrategyTest {

    private val localData = listOf(
            "local 1",
            "local 2",
            "local 3"
    )
    private val remoteData = listOf(
            "remote 1",
            "remote 2",
            "remote 3"
    )

    private val localDataSource: DataSource<String> = mock {
        on { dataOutput() } doReturn Flowable.fromIterable(localData)
    }

    private val remoteDataSource: DataSource<String> = mock {
        on { dataOutput() } doReturn Flowable.fromIterable(remoteData)
    }

    private lateinit var strategy: TestStrategy

    @Test
    fun `when only LOCAL strategy is selected, then publish only data from LOCAL data source`() {
        strategy = TestStrategy(StrategyActionType.OnlyLocal)

        val subscriber = strategy.selectDataOutput(localDataSource, remoteDataSource).test()

        subscriber.assertComplete()
        subscriber.assertValueCount(localData.size)
        subscriber.assertValues("local 1", "local 2", "local 3")
    }

    @Test
    fun `when only REMOTE strategy is selected, then publish only data from REMOTE data source`() {
        strategy = TestStrategy(StrategyActionType.OnlyRemote)

        val subscriber = strategy.selectDataOutput(localDataSource, remoteDataSource).test()

        subscriber.assertComplete()
        subscriber.assertValueCount(localData.size)
        subscriber.assertValues("remote 1", "remote 2", "remote 3")
    }

    @Test
    fun `when FirstRemoteThenLocalNoUpdate type is selected, then publish data from REMOTE first and do not call update`() {
        strategy = TestStrategy(StrategyActionType.FirstRemoteThenLocalNoUpdate)

        val subscriber = strategy.selectDataOutput(localDataSource, remoteDataSource).test()

        subscriber.assertComplete()
        subscriber.assertValueCount(localData.size + remoteData.size)
        subscriber.assertValues("remote 1", "remote 2", "remote 3", "local 1", "local 2", "local 3")
        verify(localDataSource, never()).update(any())
        verify(remoteDataSource, never()).update(any())
    }

    @Test
    fun `when FirstRemoteThenLocalWithLocalUpdate type is selected, then publish data from REMOTE first and do not call update`() {
        whenever(localDataSource.update(any())).thenReturn(Completable.complete())
        strategy = TestStrategy(StrategyActionType.FirstRemoteThenLocalWithLocalUpdate)

        val subscriber = strategy.selectDataOutput(localDataSource, remoteDataSource).test()

        subscriber.assertComplete()
        subscriber.assertValueCount(localData.size + remoteData.size)
        subscriber.assertValues("remote 1", "remote 2", "remote 3", "local 1", "local 2", "local 3")
        verify(localDataSource, times(remoteData.size)).update(any())
        verify(remoteDataSource, never()).update(any())
    }

    @Test
    fun `when FirstLocalThenRemoteWithLocalUpdate type is selected, then publish data from LOCAL first then from REMOTE and again from LOCAL, do not call update`() {
        whenever(localDataSource.update(any())).thenReturn(Completable.complete())
        strategy = TestStrategy(StrategyActionType.FirstLocalThenRemoteWithLocalUpdate)

        val subscriber = strategy.selectDataOutput(localDataSource, remoteDataSource).test()

        subscriber.assertComplete()
        subscriber.assertValueCount(localData.size + remoteData.size + localData.size)
        subscriber.assertValues("local 1", "local 2", "local 3", "remote 1", "remote 2", "remote 3", "local 1", "local 2", "local 3")
        verify(localDataSource, times(remoteData.size)).update(any())
        verify(remoteDataSource, never()).update(any())
    }

    @Test
    fun `when FirstLocalThenRemoteWithLocalUpdate type is selected, then publish data from LOCAL first and update`() {
        strategy = TestStrategy(StrategyActionType.FirstLocalThenRemoteNoLocalUpdate)

        val subscriber = strategy.selectDataOutput(localDataSource, remoteDataSource).test()

        subscriber.assertComplete()
        subscriber.assertValueCount(localData.size + remoteData.size)
        subscriber.assertValues("local 1", "local 2", "local 3", "remote 1", "remote 2", "remote 3")
        verify(localDataSource, never()).update(any())
        verify(remoteDataSource, never()).update(any())
    }
}