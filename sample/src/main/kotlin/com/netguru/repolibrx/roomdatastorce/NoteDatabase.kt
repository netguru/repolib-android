package com.netguru.repolibrx.roomdatastorce

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.netguru.repolibrx.roomdatastorce.NoteDatabase.Companion.DB_VERSION

/**
 * Definition of the [Room] database. For more information check [Room] documentation.
 */
@Database(entities = [Note::class], version = DB_VERSION)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteLocalDao

    companion object {
        const val DB_VERSION = 1
        const val NOTES_TABLE_NAME = "Notes"
    }
}
