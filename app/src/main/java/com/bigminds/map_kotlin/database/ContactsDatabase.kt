package com.bigminds.map_kotlin.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bigminds.map_kotlin.model.Contact

@Database(entities = [Contact::class], version = 1)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun contactsDao(): ContactsDao

    companion object {
        private var database: ContactsDatabase? = null

        fun getDatabase(context: Context): ContactsDatabase {
            if (database == null) {
                database = Room
                    .databaseBuilder(context, ContactsDatabase::class.java, "contactsDb")
                    .fallbackToDestructiveMigration(false)
                    .build()
            }
            return database as ContactsDatabase
        }
    }
}