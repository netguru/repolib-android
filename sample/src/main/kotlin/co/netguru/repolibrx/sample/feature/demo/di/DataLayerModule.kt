package co.netguru.repolibrx.sample.feature.demo.di

import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.initializer.createRepo
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.common.LocalRealmDataSourceQualifier
import co.netguru.repolibrx.sample.common.RemoteRetrofitDataSourceQualifier
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.datasource.DemoAppRequestStrategyFactoryFactory
import co.netguru.repolibrx.sample.feature.demo.datasource.api.API
import co.netguru.repolibrx.sample.feature.demo.datasource.api.RetrofitDataSource
import dagger.Module
import dagger.Provides

@Module
class DataLayerModule {
    //todo describe repo init
    //RepoLib SETUP
    @AppScope
    @Provides
    @RemoteRetrofitDataSourceQualifier
    fun provideRemoteDataSource(api: API)
            : DataSource<DemoDataEntity> = RetrofitDataSource(api)

    @AppScope
    @Provides
    fun provideRequestStrategyFactory(): DemoAppRequestStrategyFactoryFactory = DemoAppRequestStrategyFactoryFactory()

    @AppScope
    @Provides
    fun provideRepoLibRx(
            //todo describe usage of Realm or Room here
            @LocalRealmDataSourceQualifier localDemoDataSource: DataSource<DemoDataEntity>,
            @RemoteRetrofitDataSourceQualifier remoteDemoDataSource: DataSource<DemoDataEntity>,
            demoAppRequestStrategyFactory: DemoAppRequestStrategyFactoryFactory
    ): RepoLibRx<DemoDataEntity> = createRepo {
        localDataSourceController = localDemoDataSource
        remoteDataSourceController = remoteDemoDataSource
        requestsStrategyFactory = demoAppRequestStrategyFactory
    }
}