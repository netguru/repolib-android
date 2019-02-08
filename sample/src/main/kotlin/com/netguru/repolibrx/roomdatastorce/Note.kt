package com.netguru.repolibrx.roomdatastorce

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room

/**
 * Example of data entity compatible with [Room] database
 */
@Entity(tableName = "Notes")
data class Note(
        @PrimaryKey(autoGenerate = true) @NonNull @ColumnInfo(name = "id") val id: Int = 0,
        @NonNull @ColumnInfo(name = "value") val value: String
)