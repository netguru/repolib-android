package co.netguru.repolibrx.sample.feature.demo.di.datasources

import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.feature.demo.datasource.api.API
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class RetrofitModule {
    //todo describe retrofit init
    @AppScope
    @Provides
    fun provideGSON(): Gson = GsonBuilder().create()

    @AppScope
    @Provides
    fun provideOkHttp(interceptor: Interceptor): OkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(interceptor)
            .build()

    @AppScope
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            //todo explain why base is example
            .baseUrl("http://example.com")
            .client(okHttpClient)
            .build()

    @AppScope
    @Provides
    fun provideApi(retrofit: Retrofit): API = retrofit.create(API::class.java)
}