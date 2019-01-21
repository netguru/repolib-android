package co.netguru.repolibrx.roomadapter

import androidx.room.Insert
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.Single

interface BaseDao<D> {

    @RawQuery
    fun query(rawQuery: SupportSQLiteQuery): Single<List<D>>

    @Insert
    fun create(dataModel: D)

    @Update
    fun update(dataModel: D)

    @RawQuery
    fun delete(rawQuery: SupportSQLiteQuery): Int
}