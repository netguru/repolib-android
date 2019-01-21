package co.netguru.repolibrx.roomdatastorce

import androidx.room.Database
import androidx.room.RoomDatabase
import co.netguru.repolibrx.roomdatastorce.NoteDatabase.Companion.DB_VERSION


@Database(entities = [Note::class], version = DB_VERSION)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteLocalDao

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "database"
        const val NOTES_TABLE_NAME = "Notes"
    }
}
