package co.netguru.repolibrx.sample.feature.demo.di

import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.initializer.createRepo
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.common.LocalDataSourceQualifier
import co.netguru.repolibrx.sample.common.RemoteDataSourceQualifier
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.datasource.DemoAppRequestStrategyFactoryFactory
import co.netguru.repolibrx.sample.feature.demo.datasource.api.API
import co.netguru.repolibrx.sample.feature.demo.datasource.api.RetrofitDataSource
import co.netguru.repolibrx.sample.feature.demo.datasource.localstore.RealmDataSource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class DataLayerModule {

    //NETWORK DEPENDENCIES SETUP (OkHttp | GSON | RETROFIT)
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

    //LOCAL STORAGE DEPENDENCIES SETUP (REALM)
    @AppScope
    @Provides
    fun provideRealmConfiguration(): RealmConfiguration = RealmConfiguration.Builder()
            .name("realm database")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()


    //RepoLib SETUP
    @AppScope
    @Provides
    @LocalDataSourceQualifier
    fun provideLocalDataSource(realmConfiguration: RealmConfiguration)
            : DataSource<DemoDataEntity> = RealmDataSource(realmConfiguration)

    @AppScope
    @Provides
    @RemoteDataSourceQualifier
    fun provideRemoteDataSource(api: API)
            : DataSource<DemoDataEntity> = RetrofitDataSource(api)

    @AppScope
    @Provides
    fun provideRequestStrategyFactory(): DemoAppRequestStrategyFactoryFactory = DemoAppRequestStrategyFactoryFactory()

    @AppScope
    @Provides
    fun provideRepoLibRx(
            @LocalDataSourceQualifier localDemoDataSource: DataSource<DemoDataEntity>,
            @RemoteDataSourceQualifier remoteDemoDataSource: DataSource<DemoDataEntity>,
            demoAppRequestStrategyFactory: DemoAppRequestStrategyFactoryFactory
    ): RepoLibRx<DemoDataEntity> = createRepo {
        localDataSourceController = localDemoDataSource
        remoteDataSourceController = remoteDemoDataSource
        requestsStrategyFactory = demoAppRequestStrategyFactory
    }
}