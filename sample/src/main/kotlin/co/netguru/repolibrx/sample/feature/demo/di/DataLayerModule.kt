package co.netguru.repolibrx.sample.feature.demo.di

import androidx.room.Room
import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.RepoLibRx
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.initializer.createRepo
import co.netguru.repolibrx.realmadapter.RxRealmDataSource
import co.netguru.repolibrx.roomadapter.RxRoomDataSource
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.common.LocalRealmDataSourceQualifier
import co.netguru.repolibrx.sample.common.LocalRoomDataSourceQualifier
import co.netguru.repolibrx.sample.common.RemoteRetrofitDataSourceQualifier
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.datasource.DemoAppRequestStrategyFactoryFactory
import co.netguru.repolibrx.sample.feature.demo.datasource.api.RetrofitDataSource
import co.netguru.repolibrx.sample.feature.demo.di.datasources.RetrofitModule
import co.netguru.repolibrx.sample.feature.demo.di.datasources.RoomModule
import co.netguru.repolibrx.strategy.RequestsStrategyFactory
import dagger.Module
import dagger.Provides
import io.realm.Realm
import retrofit2.Retrofit

/**
 * [DataLayerModule] is one of the [Module]s used by Dagger to initialize components. This module
 * contains initialization of the [RepoLib].
 *
 * [<br/><br/>]
 * Initialization of the [RepoLib] took place in the following steps
 *
 * 1) initialization of the remote data source - in this case remote data source is implemented using
 * [Retrofit] library. The logic for handling data by Retrofit is placed in [RetrofitDataSource]
 * which is specific implementation of the [DataSource] interface. Initialization of [RetrofitDataSource]
 * is placed in the [RetrofitModule]
 *
 * 2)  initialization of the local data source - in this case local data source is implemented using
 * [Realm]. The logic for handling data by the Realm is implemented in the [RxRealmDataSource]
 * which is specific implementation of the [DataSource]. In contrast to [RxRealmDataSource] there is
 * no need to implement Realm Data Source on its own. Developer can use ready top use [RxRealmDataSource]
 * from co.netguru.repolibrx.realmadapter package.
 * This sample contains also implementation of the [DataSource] based on [Room]. It is also implemented
 * using Room adapter for [RepoLib] - [RxRoomDataSource]. Initialization of the [RxRoomDataSource]
 * is placed in another Dagger module [RoomModule].
 * Both [DataSource] implementations can be accessed using qualifiers: [LocalRealmDataSourceQualifier]
 * and [LocalRoomDataSourceQualifier].
 *
 * 3) implementation and initialization of the [RequestsStrategyFactory] - in this case
 * [RequestsStrategyFactory] is implemented in the [DemoAppRequestStrategyFactoryFactory].
 *
 * 4) initialization of the [RepoLib] controller using helper utility function - [createRepo].
 * Initialization requires 3 params described above.
 */
@Module
class DataLayerModule {
    @AppScope
    @Provides
    fun provideRequestStrategyFactory(): RequestsStrategyFactory = DemoAppRequestStrategyFactoryFactory()

    /**
     * This method contains initialization of the [RepoLib] using helper utility function - [createRepo].
     * All 3 parameters required by the [RepoLib] are provided by the Dagger modules. Specific local
     * [DataSource] implementation is provided with [LocalRealmDataSourceQualifier] to select
     * [RxRealmDataSource]. Initialization is using qualifiers because there are three [DataSource]
     * implementations and all of them can be used interchangeably. E.g. you can change
     * [LocalRealmDataSourceQualifier] to [LocalRoomDataSourceQualifier] to use [DataSource] based
     * on [Room] storage.
     */
    @AppScope
    @Provides
    fun provideRepoLibRx(
            @LocalRealmDataSourceQualifier localDemoDataSource: DataSource<DemoDataEntity>,
            @RemoteRetrofitDataSourceQualifier remoteDemoDataSource: DataSource<DemoDataEntity>,
            demoAppRequestStrategyFactory: RequestsStrategyFactory
    ): RepoLibRx<DemoDataEntity> = createRepo {
        localDataSource = localDemoDataSource
        remoteDataSource = remoteDemoDataSource
        requestsStrategyFactory = demoAppRequestStrategyFactory
    }
}