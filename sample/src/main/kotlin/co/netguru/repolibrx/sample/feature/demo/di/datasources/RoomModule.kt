package co.netguru.repolibrx.sample.feature.demo.di.datasources

import android.content.Context
import androidx.room.Room
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.roomadapter.RxRoomDataSource
import co.netguru.repolibrx.roomadapter.mappers.RoomDataMapper
import co.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper
import co.netguru.repolibrx.roomdatastorce.Note
import co.netguru.repolibrx.roomdatastorce.NoteDatabase
import co.netguru.repolibrx.roomdatastorce.NotesDataMapper
import co.netguru.repolibrx.roomdatastorce.NotesQueryMapper
import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.common.LocalRoomDataSourceQualifier
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import dagger.Module
import dagger.Provides

@Module
class RoomModule {
    //todo describe room init

    @AppScope
    @Provides
    fun provideQueryMapper(): RoomQueryMapper = NotesQueryMapper()

    @AppScope
    @Provides
    fun provideDataMapper(): RoomDataMapper<DemoDataEntity, Note> = NotesDataMapper()

    @AppScope
    @Provides
    fun provideNoteDataBase(context: Context): NoteDatabase = Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            NoteDatabase.NOTES_TABLE_NAME
    ).allowMainThreadQueries()
            .enableMultiInstanceInvalidation()
            .build()

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