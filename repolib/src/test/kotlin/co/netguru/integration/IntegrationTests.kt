package co.netguru.integration

import co.netguru.TestDataEntity
import co.netguru.data.Query
import co.netguru.datasource.DataSource
import co.netguru.initializer.createRemoteController
import co.netguru.initializer.createRepo
import co.netguru.strategy.DefaultSourcingStrategy
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

//todo next tests will be added later
@RunWith(MockitoJUnitRunner::class)
class IntegrationTests {

    private val localData = listOf(
            TestDataEntity(11),
            TestDataEntity(22)
    )
    private val remoteData = listOf(
            TestDataEntity(99),
            TestDataEntity(88),
            TestDataEntity(77)
    )

    private val query: Query<TestDataEntity> = mock()

    private val localDataSourceMock: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(localData)
        on { update(any()) } doReturn Completable.complete()
    }

    private val remoteDataSourceMock: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(remoteData)
    }

    private val repoLib = createRepo<TestDataEntity> {

        sourcingStrategy = DefaultSourcingStrategy()

        localDataSourceController = localDataSourceMock

        remoteDataSourceController = createRemoteController {
            this.dataSource = remoteDataSourceMock
        }
    }

    @Test
    fun `when outputDataStream() subscribed and LocalData Source emit items, then receive items from LOCAL`() {

        val dataSubscriber = repoLib.outputDataStream().test()
        val requestSubscriber = repoLib.fetch(query).test()

        requestSubscriber.assertComplete()
        dataSubscriber.assertNoErrors()
        dataSubscriber.assertValues(localData[0], localData[1])
        dataSubscriber.assertValueCount(localData.size)
        verify(remoteDataSourceMock).fetch(any())
        verify(localDataSourceMock, times(remoteData.size)).update(any())
        verify(localDataSourceMock).fetch(any())
    }
}