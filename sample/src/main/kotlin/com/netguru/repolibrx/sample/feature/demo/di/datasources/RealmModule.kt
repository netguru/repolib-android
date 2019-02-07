package com.netguru.repolibrx.sample.feature.demo.di.datasources

import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.datasource.DataSource
import com.netguru.repolibrx.realmadapter.RealmDataMapper
import com.netguru.repolibrx.realmadapter.RealmQueryMapper
import com.netguru.repolibrx.realmadapter.RxRealmDataSource
import com.netguru.repolibrx.sample.application.scope.AppScope
import com.netguru.repolibrx.sample.common.LocalRealmDataSourceQualifier
import com.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.netguru.repolibrx.sample.feature.demo.datasource.localstore.DataMapper
import com.netguru.repolibrx.sample.feature.demo.datasource.localstore.NoteLocalRealmObject
import com.netguru.repolibrx.sample.feature.demo.datasource.localstore.QueryMapper
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * [RealmModule] is an Dagger module responsible for initialization of [RxRealmDataSource] and
 * all dependencies that [RxRealmDataSource] and [Realm] need.
 */
@Module
class RealmModule {

    /**
     * [provideDataMapper] provides implementation of the [RealmDataMapper] that is used by the
     * [RxRealmDataSource] to translate [RepoLib] query objects to [RealmQuery];
     */
    @AppScope
    @Provides
    fun provideQueryMapper(): RealmQueryMapper<NoteLocalRealmObject> = QueryMapper()

    /**
     * [provideQueryMapper] provides implementation of [RealmQueryMapper] interface that is used by
     * [RxRealmDataSource] to translate data entity model to [RealmObject]s.
     */
    @AppScope
    @Provides
    fun provideDataMapper(): RealmDataMapper<DemoDataEntity, NoteLocalRealmObject> = DataMapper()

    /**
     * [provideRealmConfiguration] is responsible for initialization of [RealmConfiguration].
     * [RealmConfiguration] is required by the [Realm] to initialize and manage the database.
     * For more information check [Realm] documentation [https://realm.io/docs/java/latest]
     */
    @AppScope
    @Provides
    fun provideRealmConfiguration(): RealmConfiguration = RealmConfiguration.Builder()
            .name("realm database")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()

    /**
     * [provideLocalDataSource] is responsible for providing [RxRealmDataSource] as a implementation
     * of [DataSource]. [RxRealmDataSource] is an ready to use implementation of the [DataSource]
     * that contains implementation of basic operation on [Realm] storage to reduce boiler plate code
     * that developer need to write to fully implement [DataSource].
     *
     * @param realmConfiguration is an instance of [RealmConfiguration] object initialized in the
     * [provideRealmConfiguration] method.
     * @param dataMapper instance of [RealmQueryMapper] used by the [RxRealmDataSource] to translate
     * [RepoLib] queries to SQL queries required by the [Realm] storage.
     * @param queryMapper instance of [RealmDataMapper] used to translate data entity model
     * to Room data models.
     */
    @AppScope
    @Provides
    @LocalRealmDataSourceQualifier
    fun provideLocalDataSource(
            realmConfiguration: RealmConfiguration,
            dataMapper: RealmDataMapper<DemoDataEntity, NoteLocalRealmObject>,
            queryMapper: RealmQueryMapper<NoteLocalRealmObject>
    ): DataSource<DemoDataEntity> = RxRealmDataSource(realmConfiguration, dataMapper, queryMapper)
}