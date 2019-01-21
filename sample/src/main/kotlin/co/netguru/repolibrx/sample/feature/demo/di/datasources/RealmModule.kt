package co.netguru.repolibrx.sample.feature.demo.di.datasources

import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.realmadapter.RealmDataMapper
import co.netguru.repolibrx.realmadapter.RealmQueryMapper
import co.netguru.repolibrx.realmadapter.RxRealmDataSource
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.common.LocalRealmDataSourceQualifier
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.datasource.localstore.DataMapper
import co.netguru.repolibrx.sample.feature.demo.datasource.localstore.NoteLocalRealmObject
import co.netguru.repolibrx.sample.feature.demo.datasource.localstore.QueryMapper
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration

@Module
class RealmModule {
    //todo describe
    @AppScope
    @Provides
    fun provideDataMapper(): RealmDataMapper<DemoDataEntity, NoteLocalRealmObject> = DataMapper()

    @AppScope
    @Provides
    fun provideQueryMapper(): RealmQueryMapper<NoteLocalRealmObject> = QueryMapper()

    @AppScope
    @Provides
    fun provideRealmConfiguration(): RealmConfiguration = RealmConfiguration.Builder()
            .name("realm database")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()

    @AppScope
    @Provides
    @LocalRealmDataSourceQualifier
    //    todo refactor
    fun provideLocalDataSource(realmConfiguration: RealmConfiguration)
            : DataSource<DemoDataEntity> {

        return RxRealmDataSource(realmConfiguration, DataMapper(), QueryMapper())
    }
}