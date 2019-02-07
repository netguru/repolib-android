package com.netguru.repolibrx.sample.feature.demo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.netguru.repolibrx.RepoLibRx
import com.netguru.repolibrx.RxTestSchedulerOverrideRule
import com.netguru.repolibrx.data.Query
import com.netguru.repolibrx.data.QueryWithParams
import com.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DemoViewModelTest {

    @Rule
    @JvmField
    val txTestRule = RxTestSchedulerOverrideRule()
    @Rule
    @JvmField
    val lifeDataTestRule = InstantTaskExecutorRule()

    private val rxRepoLibRx: RepoLibRx<DemoDataEntity> = mock {
        on { create(any()) } doReturn (Completable.complete())
        on { fetch(any()) } doReturn (Completable.complete())
        on { delete(any()) } doReturn (Completable.complete())
    }
    private val demoViewModel = DemoViewModel(rxRepoLibRx)
    private val demoDataEntity: DemoDataEntity = mock()

    @Test
    fun `when edit event sent, then publish editLiveData object to open editor`() {
        val editEventSubject = PublishSubject.create<DemoDataEntity>()
        demoViewModel.setupUpdatingAction(editEventSubject)


        editEventSubject.onNext(demoDataEntity)

        var result: DemoDataEntity? = null
        demoViewModel.editDataLiveData.observeForever {
            result = it
        }
        Assert.assertNotNull(result)
        Assert.assertEquals(demoDataEntity, result)
    }

    @Test
    fun `when addNew() method called, then CREATE entity with new value to the repoLib`() {
        val testValue = "new"
        val argumentCaptor = argumentCaptor<DemoDataEntity>()

        demoViewModel.addNew(testValue)

        verify(rxRepoLibRx).create(argumentCaptor.capture())
        Assert.assertEquals(testValue, argumentCaptor.firstValue.value)
        demoViewModel.stateLiveData.observeForever {
            Assert.assertNull(it.error)
        }
    }

    @Test
    fun `when repoLib return Throwable on create() method call, then show error using stateLiveData `() {
        val testValue = "new"
        val errorMessage = "test error"
        val throwable = Throwable(errorMessage)
        val argumentCaptor = argumentCaptor<DemoDataEntity>()
        whenever(rxRepoLibRx.create(any())).thenReturn(Completable.error(throwable))

        demoViewModel.addNew(testValue)

        verify(rxRepoLibRx).create(argumentCaptor.capture())
        Assert.assertEquals(testValue, argumentCaptor.firstValue.value)
        demoViewModel.stateLiveData.observeForever {
            Assert.assertNotNull(it.error)
            Assert.assertEquals(errorMessage, it.error)
        }
    }

    @Test
    fun `when refresh() is called, then call fetch on repoLib`() {
        val argumentCaptor = argumentCaptor<Query>()

        demoViewModel.refresh()

        verify(rxRepoLibRx).fetch(argumentCaptor.capture())
        demoViewModel.stateLiveData.observeForever {
            Assert.assertNull(it.error)
        }
    }

    @Test
    fun `when repoLib return Throwable on fetch() method call, then show error using stateLiveData `() {
        val errorMessage = "test error"
        val throwable = Throwable(errorMessage)
        val argumentCaptor = argumentCaptor<Query>()
        whenever(rxRepoLibRx.fetch(any())).thenReturn(Completable.error(throwable))

        demoViewModel.refresh()

        verify(rxRepoLibRx).fetch(argumentCaptor.capture())
        demoViewModel.stateLiveData.observeForever {
            Assert.assertNotNull(it.error)
            Assert.assertEquals(errorMessage, it.error)
        }
    }

    @Test
    fun `when remove event received, then call remove on repoLib and publish item removeLiveData`() {
        val removeSubject = PublishSubject.create<DemoDataEntity>()
        demoViewModel.setupRemovingAction(removeSubject)
        val argumentCaptor = argumentCaptor<QueryWithParams>()

        removeSubject.onNext(demoDataEntity)

        verify(rxRepoLibRx).delete(argumentCaptor.capture())
        Assert.assertEquals(demoDataEntity.id, argumentCaptor.firstValue.param("id"))
        demoViewModel.removedItemLiveData.observeForever {
            Assert.assertNotNull(it)
            Assert.assertEquals(demoDataEntity, it)
        }
    }

    @Test
    fun `when remove event received and repoLib returns error, then show error using stateLiveData`() {
        val removeSubject = PublishSubject.create<DemoDataEntity>()
        val argumentCaptor = argumentCaptor<QueryWithParams>()
        val errorMessage = "test error"
        val throwable = Throwable(errorMessage)
        whenever(rxRepoLibRx.delete(any())).thenReturn(Completable.error(throwable))

        demoViewModel.setupRemovingAction(removeSubject)
        removeSubject.onNext(demoDataEntity)

        verify(rxRepoLibRx).delete(argumentCaptor.capture())
        Assert.assertEquals(demoDataEntity.id, argumentCaptor.firstValue.param("id"))
        demoViewModel.stateLiveData.observeForever {
            Assert.assertNotNull(it.error)
            Assert.assertEquals(errorMessage, it.error)
        }
    }

    @Test
    fun `when repoLib return data on output stream, then publish it to view using updateLiveData`() {
        val dataArray = arrayListOf(
                DemoDataEntity(1, "test 1"),
                DemoDataEntity(2, "test 2"),
                DemoDataEntity(3, "test 3")
        )
        whenever(rxRepoLibRx.outputDataStream()).thenReturn(Flowable.fromIterable(dataArray))
        val resultArray = mutableListOf<DemoDataEntity>()

        demoViewModel.getData { resultArray += it }

        Assert.assertArrayEquals(dataArray.toTypedArray(), resultArray.toTypedArray())
    }
}
