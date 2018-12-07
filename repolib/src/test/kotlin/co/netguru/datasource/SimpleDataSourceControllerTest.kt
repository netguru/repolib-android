package co.netguru.datasource

import co.netguru.TestDataEntity
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
class SimpleDataSourceControllerTest {

    private val testDataEntity = TestDataEntity(99, "test")
    private val query = Query<TestDataEntity>()
    private val dataList = mutableListOf(testDataEntity)
    private val argumentCaptor = argumentCaptor<Request<TestDataEntity>>()

    private val dataSource: DataSource<TestDataEntity> = mock {
        on { fetch(any()) } doReturn Flowable.fromIterable(dataList)
        on { create(any()) } doReturn Completable.complete()
        on { delete(any()) } doReturn Completable.complete()
        on { update(any()) } doReturn Completable.complete()
    }

    @Spy
    private val dataSourceController = SimpleDataSourceController(dataSource)

    @Test
    fun `when getData() subscribed and fetch() completed then return data`() {
        dataList.add(testDataEntity.copy(id = 88))
        dataList.add(testDataEntity.copy(id = 77))

        val testSubscriber = dataSourceController.dataOutput().test()
        val fetchSubscriber = dataSourceController.fetch(query).test()

        testSubscriber.assertValues(dataList[0], dataList[1], dataList[2])
        testSubscriber.assertValueCount(dataList.size)
        fetchSubscriber.assertComplete()
        fetchSubscriber.assertNoValues()
    }

    @Test
    fun `when getData() subscribed and fetch() called twice then return data twice`() {
        dataList.add(testDataEntity.copy(id = 88))
        dataList.add(testDataEntity.copy(id = 77))

        val testSubscriber = dataSourceController.dataOutput().test()
        val fetchSubscriber = dataSourceController.fetch(query).andThen(dataSourceController.fetch(query)).test()

        testSubscriber.assertValues(dataList[0], dataList[1], dataList[2], dataList[0], dataList[1], dataList[2])
        testSubscriber.assertValueCount(dataList.size * 2)
        fetchSubscriber.assertComplete()
        fetchSubscriber.assertNoValues()
    }

    @Test
    fun `when fetch() subscribed then executeQuery() is called and complete`() {

        val fetchSubscribed = dataSourceController.fetch(query).test()

        fetchSubscribed.assertComplete()
        verify(dataSource, times(1)).fetch(argumentCaptor.capture())
        Assert.assertEquals(RequestType.FETCH, argumentCaptor.firstValue.requestType)
        Assert.assertEquals(query, argumentCaptor.firstValue.query)
    }

    @Test
    fun `when delete() subscribed then executeDelete() is called and complete`() {

        val fetchSubscribed = dataSourceController.delete(query).test()

        fetchSubscribed.assertComplete()
    }

    @Test
    fun `when update() subscribed then executeUpdate() is called and complete`() {

        val fetchSubscribed = dataSourceController.update(testDataEntity).test()

        fetchSubscribed.assertComplete()
    }

    @Test
    fun `when create() subscribed then execute create`() {

        val testSubscribed = dataSourceController.create(testDataEntity).test()

        testSubscribed.assertComplete()
    }

    @Test
    fun `when fetch() called with query then data is published on dataOutput() stream`() {
        dataList.add(testDataEntity.copy(id = 88))
        dataList.add(testDataEntity.copy(id = 77))

        val dataSubscriber = dataSourceController.dataOutput().test()
        val fetchSubscriber = dataSourceController.fetch(query).test()

        dataSubscriber.assertValueCount(dataList.size)
        dataSubscriber.assertValues(dataList[0], dataList[1], dataList[2])
        fetchSubscriber.assertComplete()
        verify(dataSource).fetch(argumentCaptor.capture())
        Assert.assertEquals(RequestType.FETCH, argumentCaptor.firstValue.requestType)
        Assert.assertEquals(query, argumentCaptor.firstValue.query)
    }

    @Test
    fun `when fetch() called with query and query return exception then publish it on both streams`() {
        val error = Throwable()
        whenever(dataSource.fetch(any())).thenReturn(Flowable.error(error))

        val dataSubscriber = dataSourceController.dataOutput().test()
        val fetchSubscriber = dataSourceController.fetch(query).test()

        dataSubscriber.assertError(error)
        fetchSubscriber.assertError(error)
        verify(dataSource).fetch(argumentCaptor.capture())
        Assert.assertEquals(RequestType.FETCH, argumentCaptor.firstValue.requestType)
        Assert.assertEquals(query, argumentCaptor.firstValue.query)
    }
}