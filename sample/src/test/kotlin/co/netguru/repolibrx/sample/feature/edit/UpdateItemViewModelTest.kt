package co.netguru.repolibrx.sample.feature.edit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.RxTestSchedulerOverrideRule
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpdateItemViewModelTest {

    @Rule
    @JvmField
    val txTestRule = RxTestSchedulerOverrideRule()
    @Rule
    @JvmField
    val lifeDataTestRule = InstantTaskExecutorRule()

    private val repoLibRx: RepoLibRx<DemoDataEntity> = mock {
        on { update(any()) } doReturn (Completable.complete())
    }
    private val updateItemViewModel = UpdateItemViewModel(repoLibRx)
    private val mockDemoDataEntity: DemoDataEntity = mock()

    @Spy
    val onComplete: () -> Unit = {}

    @Spy
    val onError: (Throwable) -> Unit = {}

    @Test
    fun `when update called and complete, then call update on repoLib `() {

        updateItemViewModel.update(
                mockDemoDataEntity,
                onCompleteAction = onComplete,
                onError = onError
        )

        verify(repoLibRx).update(mockDemoDataEntity)
        verify(onComplete).invoke()
        verify(onError, never()).invoke(any())
    }

    @Test
    fun `when update called and fail, then call update on repoLib and return error`() {
        val throwable = Throwable("test")
        whenever(repoLibRx.update(any())).thenReturn(Completable.error(throwable))

        updateItemViewModel.update(
                mockDemoDataEntity,
                onCompleteAction = onComplete,
                onError = onError
        )

        verify(repoLibRx).update(mockDemoDataEntity)
        verify(onComplete, never()).invoke()
        verify(onError).invoke(throwable)
    }
}