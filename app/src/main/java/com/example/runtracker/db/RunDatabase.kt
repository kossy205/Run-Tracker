package com.example.runtracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class RunDatabase: RoomDatabase() {

    abstract fun getRunDao(): RunDao
}