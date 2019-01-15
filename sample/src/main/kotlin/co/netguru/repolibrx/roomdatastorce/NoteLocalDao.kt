package co.netguru.repolibrx.roomdatastorce

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import co.netguru.repolibrx.roomadapter.BaseDao
import io.reactivex.Single

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
