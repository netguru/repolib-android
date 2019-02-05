package co.netguru.repolibrx.roomdatastorce

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import co.netguru.repolibrx.roomadapter.BaseDao
import co.netguru.repolibrx.roomadapter.RxRoomDataSource
import io.reactivex.Single

/**
 * Example implementation of the Dao interface that is used by the [RxRoomDataSource].
 * To make such specific Dao class compatible with the [RxRoomDataSource] its need to extend
 * [BaseDao] interface.
 */
@Dao
interface NoteLocalDao : BaseDao<Note> {

    @RawQuery
    override fun query(rawQuery: SupportSQLiteQuery): Single<List<Note>>

    @Insert
    override fun create(dataModel: Note)

    @Update
    override fun update(dataModel: Note)

    @RawQuery
    override fun delete(rawQuery: SupportSQLiteQuery): Int
}
