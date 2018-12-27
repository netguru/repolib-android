package co.netguru.repolib.feature.demo.di

import co.netguru.repolib.application.scope.AppScope
import co.netguru.repolib.common.LocalDataSourceQualifier
import co.netguru.repolib.common.RemoteDataSourceQualifier
import co.netguru.repolib.feature.demo.data.DataEntity
import co.netguru.repolib.feature.demo.datasource.api.API
import co.netguru.repolib.feature.demo.datasource.api.RetrofitDataSource
import co.netguru.repolib.feature.demo.datasource.localstore.RealmDataSource
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.initializer.createRepo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
class RepositoryModule {

    //NETWORK DEPENDENCIES SETUP (OkHttp | GSON | RETROFIT)
    @AppScope
    @Provides
    fun provideOKHttp() = OkHttpClient.Builder().build()

    @AppScope
    @Provides
    fun provideGson() = GsonBuilder().create()

    @AppScope
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson) = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            //todo
            .baseUrl("http://example.com")
            .client(okHttpClient)
            .build()

    @AppScope
    @Provides
    fun provideApi(retrofit: Retrofit) = retrofit.create(API::class.java)

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
            : DataSource<DataEntity> = RealmDataSource(realmConfiguration)

    @AppScope
    @Provides
    @RemoteDataSourceQualifier
    fun provideRemoteDataSource(api: API)
            : DataSource<DataEntity> = RetrofitDataSource(api)

    @AppScope
    @Provides
    fun provideRepoLibRx(
            @LocalDataSourceQualifier localDataSource: DataSource<DataEntity>,
            @RemoteDataSourceQualifier remoteDataSource: DataSource<DataEntity>
    ): RepoLibRx<DataEntity> = createRepo {
        localDataSourceController = localDataSource
        remoteDataSourceController = remoteDataSource
    }
}