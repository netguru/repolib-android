package com.netguru.repolibrx.sample.feature.demo.datasource.api

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MockingInterceptorTest {

    companion object {
        const val PREDEFINED_ITEMS_COUNT: Int = 5
    }

    private val gson = Gson()
    private val chainMock: Interceptor.Chain = mock()
    private val networkInfoMock: NetworkInfo = mock {
        on { isConnected } doReturn (true)
    }
    private val connectivityManager: ConnectivityManager = mock {
        on { activeNetworkInfo } doReturn (networkInfoMock)
    }
    private val mockingInterceptor = MockingInterceptor(gson, connectivityManager)

    @Test
    fun whenGetAllRequestSent_thenReturnListOfPredefinedElements() {
        //givengit
        val getAllRequest = Request.Builder()
                .url("http://example.com/getAll")
                .build()
        whenever(chainMock.request()).thenReturn(getAllRequest)

        //when
        val response = mockingInterceptor.intercept(chainMock)

        //then
        val listOfItems: List<DemoDataEntity> = gson.fromJson(
                response.body()?.string(),
                object : TypeToken<List<DemoDataEntity>>() {}.type
        )
        Assert.assertTrue(response.isSuccessful)
        Assert.assertEquals(200, response.code())
        Assert.assertEquals(PREDEFINED_ITEMS_COUNT, listOfItems.size)
    }

    @Test
    fun whenCreateRequestSent_thenCreatedObjectAndAddItToAll() {
        //given
        val newItem = DemoDataEntity(value = "new item")
        val getAllRequest = Request.Builder().url("http://example.com/getAll").build()
        val createRequest = Request.Builder()
                .url("http://example.com/create")
                .method(
                        "POST",
                        RequestBody.create(
                                MediaType.parse("application/json"),
                                gson.toJson(newItem)
                        )
                ).build()
        whenever(chainMock.request()).thenReturn(getAllRequest)

        //when
        val createResponse = mockingInterceptor.intercept(mock {
            on { request() } doReturn createRequest
        })
        val getAllResponse = mockingInterceptor.intercept(chainMock)

        //then
        val listOfItems: List<DemoDataEntity> = gson.fromJson(
                getAllResponse.body()?.string(),
                object : TypeToken<List<DemoDataEntity>>() {}.type
        )
        val createdItem = gson.fromJson(createResponse.body()?.string(), DemoDataEntity::class.java)
        Assert.assertTrue(createResponse.isSuccessful)
        Assert.assertEquals(200, createResponse.code())
        Assert.assertTrue(getAllResponse.isSuccessful)
        Assert.assertEquals(200, getAllResponse.code())
        Assert.assertEquals(PREDEFINED_ITEMS_COUNT + 1, listOfItems.size)
        Assert.assertNotNull(listOfItems.find { it.value == newItem.value })
        Assert.assertEquals(newItem.value, createdItem.value)
    }

    @Test
    fun whenDeleteRequestSent_thenDeletedObject() {
        //given
        val idToRemove = 4.toLong()
        val getAllRequest = Request.Builder().url("http://example.com/getAll").build()
        val createRequest = Request.Builder()
                .url("http://example.com/sqlDelete?id=$idToRemove")
                .method(
                        "POST",
                        RequestBody.create(
                                MediaType.parse("application/json"),
                                ""
                        )
                ).build()
        whenever(chainMock.request()).thenReturn(getAllRequest)

        //when
        val deleteResponse = mockingInterceptor.intercept(mock {
            on { request() } doReturn createRequest
        })
        val getAllResponse = mockingInterceptor.intercept(chainMock)

        //then
        val listOfItems: List<DemoDataEntity> = gson.fromJson(
                getAllResponse.body()?.string(),
                object : TypeToken<List<DemoDataEntity>>() {}.type
        )
        Assert.assertTrue(deleteResponse.isSuccessful)
        Assert.assertEquals(200, deleteResponse.code())
        Assert.assertTrue(getAllResponse.isSuccessful)
        Assert.assertEquals(200, getAllResponse.code())
        Assert.assertEquals(PREDEFINED_ITEMS_COUNT - 1, listOfItems.size)
        Assert.assertNull(listOfItems.find { it.id == idToRemove })
    }

    @Test
    fun whenUpdateRequestSent_thenUpdateObjectOnList() {
        //given
        val itemToUpdate = DemoDataEntity(id = 2, value = "updated text")
        val getAllRequest = Request.Builder().url("http://example.com/getAll").build()
        val update = Request.Builder()
                .url("http://example.com/update")
                .method(
                        "POST",
                        RequestBody.create(
                                MediaType.parse("application/json"),
                                gson.toJson(itemToUpdate)
                        )
                ).build()
        whenever(chainMock.request()).thenReturn(getAllRequest)

        //when
        val createResponse = mockingInterceptor.intercept(mock {
            on { request() } doReturn update
        })
        val getAllResponse = mockingInterceptor.intercept(chainMock)

        //then
        val listOfItems: List<DemoDataEntity> = gson.fromJson(
                getAllResponse.body()?.string(),
                object : TypeToken<List<DemoDataEntity>>() {}.type
        )
        Assert.assertTrue(createResponse.isSuccessful)
        Assert.assertEquals(200, createResponse.code())
        Assert.assertTrue(getAllResponse.isSuccessful)
        Assert.assertEquals(200, getAllResponse.code())
        Assert.assertEquals(PREDEFINED_ITEMS_COUNT, listOfItems.size)
        val updatedItem = listOfItems.find { it.id == itemToUpdate.id }
        Assert.assertNotNull(updatedItem)
        Assert.assertEquals(itemToUpdate.value, updatedItem?.value)
    }
}