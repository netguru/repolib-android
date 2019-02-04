package co.netguru.repolibrx.sample.feature.demo.di.datasources

import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.common.RemoteRetrofitDataSourceQualifier
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.datasource.api.API
import co.netguru.repolibrx.sample.feature.demo.datasource.api.MockingInterceptor
import co.netguru.repolibrx.sample.feature.demo.datasource.api.RetrofitDataSource
import co.netguru.repolibrx.sample.feature.demo.di.MockingModule
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * [RetrofitModule] is a Dagger module that contains methods responsible for providing all components
 * related to the [Retrofit].
 */
@Module
class RetrofitModule {

    /**
     * [Gson] is used by the Retrofit to translate JSON to the Data models
     */
    @AppScope
    @Provides
    fun provideGSON(): Gson = GsonBuilder().create()

    /**
     * [OkHttpClient] is an HTTP client used by [Retrofit] to handle HTTP communication.
     *
     * @param interceptor is na interface that is used by [OkHttpClient] to intercept all HTTP requests
     * and modify them before there are sent. In this case [Interceptor] is used to mock the
     * REST API service. Initialization and description is placed in [MockingModule]
     */
    @AppScope
    @Provides
    fun provideOkHttp(interceptor: Interceptor): OkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(interceptor)
            .build()

    /**
     * [provideRetrofit] is responsible for providing instance of the [Retrofit]. Initialization
     * is based on [okHttpClient] and [gson] provided by the methods places above.
     *
     * [Retrofit] requires to provide base url for API web service. In this case
     * "http://example.com" is set as base url. The REST API service is mocked using [MockingInterceptor]
     * so the base url is not relevant.
     *
     */
    @AppScope
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("http://example.com")
            .client(okHttpClient)
            .build()

    /**
     * [provideApi] method is responsible for initialization of the [API] interface that is representing
     * REST API service.
     *
     * @param retrofit is an instance of [Retrofit] initialized in the [provideRetrofit] method
     */
    @AppScope
    @Provides
    fun provideApi(retrofit: Retrofit): API = retrofit.create(API::class.java)


    /**
     * [provideRemoteDataSource] method is responsible for Initialization of the [RetrofitDataSource]
     * used by the [RepoLib]
     *
     * @param api is an instance of the [API] interface that represents REST API service.
     */
    @AppScope
    @Provides
    @RemoteRetrofitDataSourceQualifier
    fun provideRemoteDataSource(api: API)
            : DataSource<DemoDataEntity> = RetrofitDataSource(api)
}