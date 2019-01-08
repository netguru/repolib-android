package co.netguru.repolibrx.sample.feature.demo.di

import android.content.Context
import android.net.ConnectivityManager
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.feature.demo.datasource.api.MockingInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor

@Module
class MockingModule(val context: Context) {

    @AppScope
    @Provides
    fun provideConnectivityManager() = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @AppScope
    @Provides
    //todo description of the Mocking interceptor
    fun provideInterceptor(gson: Gson, connectivityManager: ConnectivityManager)
            : Interceptor = MockingInterceptor(gson, connectivityManager)
}