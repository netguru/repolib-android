package co.netguru.datasource

import co.netguru.TestDataEntity
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DataSourceTest {

    private val testDataEntity = TestDataEntity(99, "test")
    private val query = object : Query<TestDataEntity> {}
    private val dataList = mutableListOf(testDataEntity)

    @Spy
    private val dataSource = TestDataSource(dataList)

    @Test
    fun `when getData() subscribed and fetch() completed then return data`() {
        dataList.add(testDataEntity.copy(id = 88))
        dataList.add(testDataEntity.copy(id = 77))

        val testSubscriber = dataSource.dataOutput().test()
        val fetchSubscriber = dataSource.fetch(query).test()

        testSubscriber.assertValues(dataList[0], dataList[1], dataList[2])
        testSubscriber.assertValueCount(dataList.size)
        fetchSubscriber.assertComplete()
        fetchSubscriber.assertNoValues()
    }

    @Test
    fun `when getData() subscribed and fetch() called twice then return data twice`() {
        dataList.add(testDataEntity.copy(id = 88))
        dataList.add(testDataEntity.copy(id = 77))

        val testSubscriber = dataSource.dataOutput().test()
        val fetchSubscriber = dataSource.fetch(query).andThen(dataSource.fetch(query)).test()

        testSubscriber.assertValues(dataList[0], dataList[1], dataList[2], dataList[0], dataList[1], dataList[2])
        testSubscriber.assertValueCount(dataList.size * 2)
        fetchSubscriber.assertComplete()
        fetchSubscriber.assertNoValues()
    }

    @Test
    fun `when fetch() subscribed then executeQuery() is called and complete`() {

        val fetchSubscribed = dataSource.fetch(query).test()

        fetchSubscribed.assertComplete()
        verify(dataSource, times(1)).query(query)
    }

    @Test
    fun `when delete() subscribed then executeDelete() is called and complete`() {

        val fetchSubscribed = dataSource.delete(query).test()

        fetchSubscribed.assertComplete()
    }

    @Test
    fun `when update() subscribed then executeUpdate() is called and complete`() {

        val fetchSubscribed = dataSource.update(testDataEntity).test()

        fetchSubscribed.assertComplete()
    }

    @Test
    fun `when create() subscribed then execute create`() {

        val testSubscribed = dataSource.create(testDataEntity).test()

        testSubscribed.assertComplete()
    }
}