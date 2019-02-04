package co.netguru.repolibrx.sample.feature.demo.di

import android.content.Context
import android.net.ConnectivityManager
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.feature.demo.datasource.api.MockingInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor

/**
 * [MockingModule] is Dagger module responsible for initialization of [MockingInterceptor]
 */
@Module
class MockingModule {

    /**
     * [provideConnectivityManager] is responsible for providing [ConnectivityManager] that
     * is used by the [ConnectivityManager] to simulate connection errors.
     */
    @AppScope
    @Provides
    fun provideConnectivityManager(context: Context) = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * [provideInterceptor] is responsible for providing [MockingInterceptor]. [MockingInterceptor]
     * is used to mock REST API service responses.
     *
     * @param gson instance of Gson converter
     * @param connectivityManager used by the [MockingInterceptor] to simulate connection errors
     */
    @AppScope
    @Provides
    fun provideInterceptor(gson: Gson, connectivityManager: ConnectivityManager)
            : Interceptor = MockingInterceptor(gson, connectivityManager)
}