package com.netguru.repolibrx.data

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QueryWithParamsTest {

    @Test
    fun `when query created with three params with diffrent types, then all of them are available`() {
        val query = QueryWithParams(
                Pair("key 1", "value 1"),
                Pair("key 2", 2.3),
                Pair("key 3", 4L)
        )

        val stringValue = query.param<String>("key 1")
        val floatValue = query.param<Double>("key 2")
        val floatLong = query.param<Long>("key 3")

        Assert.assertEquals("value 1", stringValue)
        Assert.assertEquals(2.3, floatValue, 0.0)
        Assert.assertEquals(4, floatLong)
    }

    @Test
    fun `when query created with three params with different types and one param added manually, then all of them are available`() {
        val query = QueryWithParams(
                Pair("key 1", "value 1"),
                Pair("key 2", 2.3),
                Pair("key 3", 4L)
        )
        val listOfParams = listOf<String>()
        query.params["key 4"]= listOfParams

        val stringValue = query.param<String>("key 1")
        val doubleValue = query.param<Double>("key 2")
        val longValue = query.param<Long>("key 3")
        val listValue = query.param<List<String>>("key 4")

        Assert.assertEquals("value 1", stringValue)
        Assert.assertEquals(2.3, doubleValue, 0.0)
        Assert.assertEquals(4, longValue)
        Assert.assertEquals(listOfParams, listValue)
    }
}