package co.netguru.integration

import co.netguru.TestDataEntity
import co.netguru.TestSourcingStrategy
import co.netguru.cache.RequestQueue
import co.netguru.data.Query
import co.netguru.datasource.DataSource
import co.netguru.initializer.createLocalController
import co.netguru.initializer.createRemoteControllerWithDefaultQueueStrategy
import co.netguru.initializer.createRepo
import co.netguru.strategy.StrategyType
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

//todo next tests will be added later
@RunWith(MockitoJUnitRunner::class)
class IntegrationTests {


    private val localDataSourceMock: DataSource<TestDataEntity> = mock()

    private val remoteDataSourceMock: DataSource<TestDataEntity> = mock()

    private val requestQueueMock: RequestQueue<TestDataEntity> = mock()

    private val query: Query<TestDataEntity> = mock()

    private val repoLib = createRepo<TestDataEntity> {

        sourcingStrategy = TestSourcingStrategy(
                StrategyType.Source.Local,
                StrategyType.Fetch.OnlyLocal
        )

        localDataSourceController = createLocalController { dataSource = localDataSourceMock }

        remoteDataSourceController = createRemoteControllerWithDefaultQueueStrategy {
            this.requestQueue = requestQueueMock
            this.dataSource = remoteDataSourceMock
        }
    }

    @Test
    fun `when outputDataStream() subscribed and LocalData Source emitted item, then receive items from LOCAL`() {
        val items = listOf(TestDataEntity(11), TestDataEntity(22))
        whenever(localDataSourceMock.fetch(any())).thenReturn(Flowable.fromIterable(items))

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.fetch(query).test()

        requestSubscriber.assertComplete()
        dataSubscriber.assertNoErrors()
        dataSubscriber.assertValueCount(items.size)
        dataSubscriber.assertValues(items[0], items[1])
    }
}