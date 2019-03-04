package com.netguru.repolibrx.sample.feature.demo.di.datasources

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.netguru.repolibrx.RepoLib
import com.netguru.repolibrx.datasource.DataSource
import com.netguru.repolibrx.roomadapter.RxRoomDataSource
import com.netguru.repolibrx.roomadapter.mappers.RoomDataMapper
import com.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper
import com.netguru.repolibrx.sample.application.scope.AppScope
import com.netguru.repolibrx.sample.common.LocalRoomDataSourceQualifier
import com.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import com.netguru.repolibrx.sample.feature.demo.datasource.roomdatastorce.Note
import com.netguru.repolibrx.sample.feature.demo.datasource.roomdatastorce.NoteDatabase
import com.netguru.repolibrx.sample.feature.demo.datasource.roomdatastorce.NotesDataMapper
import com.netguru.repolibrx.sample.feature.demo.datasource.roomdatastorce.NotesQueryMapper
import dagger.Module
import dagger.Provides

/**
 * [RoomModule] is an Dagger module responsible for initialization of [RxRoomDataSource] and
 * all dependencies that [RxRoomDataSource] and [Room] need.
 *
 * This is just an example module - it is not used in this Sample app - but it allows to illustrate
 * how to initialize [RxRoomDataSource] using Dagger DI engine.
 */
@Module
class RoomModule {

    /**
     * [provideQueryMapper] provides implementation of the [RoomQueryMapper] that is used by the
     * [RxRoomDataSource] to translate queries to SQL queries required by the [Room] storage.
     */
    @AppScope
    @Provides
    fun provideQueryMapper(): RoomQueryMapper = NotesQueryMapper()

    /**
     * [provideDataMapper] provides implementation of [RoomDataMapper] interface that is used by
     * [RxRoomDataSource] to translate data entity model to Room data models.
     */
    @AppScope
    @Provides
    fun provideDataMapper(): RoomDataMapper<DemoDataEntity, Note> = NotesDataMapper()

    /**
     * [provideNoteDataBase] provides ready to use instance of [Room] database. The database is initialized
     * according to the [Room] documentation using [RoomDatabase.Builder]
     * [https://developer.android.com/reference/android/arch/persistence/room/RoomDatabase.Builder]
     */
    @AppScope
    @Provides
    fun provideNoteDataBase(context: Context): NoteDatabase = Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            NoteDatabase.NOTES_TABLE_NAME
    ).allowMainThreadQueries()
            .enableMultiInstanceInvalidation()
            .build()

    /**
     * [provideRoomDataSource] is responsible for providing [RxRoomDataSource] as a implementation
     * of [DataSource]. [RxRoomDataSource] is an ready to use implementation of the [DataSource]
     * that contains implementation of basic operation on [Room] storage to reduce boiler plate code
     * that developer need to write to fully implement [DataSource]. [RxRoomDataSource] also requires
     * to pass name of the SQL table, same name that was passed to [Room.databaseBuilder] in
     * [provideNoteDataBase] method.
     *
     * @param noteDatabase example of [RoomDatabase] provided by the [provideNoteDataBase] method
     * @param roomDataMapper instance of [RoomQueryMapper] used by the [RxRoomDataSource] to translate
     * [RepoLib] queries to SQL queries required by the [Room] storage.
     * @param roomDataMapper instance of [RoomDataMapper] used to translate data entity model
     * to Room data models.
     */
    @AppScope
    @Provides
    @LocalRoomDataSourceQualifier
    fun provideRoomDataSource(
            noteDatabase: NoteDatabase,
            roomQueryMapper: RoomQueryMapper,
            roomDataMapper: RoomDataMapper<DemoDataEntity, Note>
    ): DataSource<DemoDataEntity> = RxRoomDataSource(
            NoteDatabase.NOTES_TABLE_NAME,
            noteDatabase.noteDao(),
            roomQueryMapper,
            roomDataMapper
    )
}