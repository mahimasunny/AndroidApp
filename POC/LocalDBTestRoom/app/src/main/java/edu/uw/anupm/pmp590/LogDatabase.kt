package edu.uw.anupm.pmp590

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch

@Database(entities = [LogData::class], version = 2)
abstract class LogDatabase : RoomDatabase() {

    abstract fun logdataDao(): LogDataDao

    companion object {
        // @Volatile
        private var INSTANCE: LogDatabase? = null

        fun getDatabase(
            context: Context //,
            // scope: CoroutineScope
        ): LogDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            LogDatabase::class.java,
                            "logdata_table"
                            )
                    .fallbackToDestructiveMigration()
                    .addCallback(LogDatabaseCallback() ) //scope))
                    .build();
            }
            return INSTANCE!!;
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    LogDatabase::class.java,
//                    "logdata_table"
//                )
//                    // Wipes and rebuilds instead of migrating if no Migration object.
//                    // Migration is not part of this codelab.
//                    // .fallbackToDestructiveMigration()
//                    // .addCallback(LogDatabaseCallback() ) //scope))
//                    .build();
//                INSTANCE = instance
//                // return instance
//                instance
//            }
        }

        private class LogDatabaseCallback(
            // private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            /**
             * Override the onOpen method to populate the database.
             * For this sample, we clear the database every time it is created or opened.
             */
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                 // If you want to keep the data through app restarts,
                 // comment out the following line.
//                 INSTANCE?.let { database ->
//                    scope.launch(Dispatchers.IO) {
//                         populateDatabase(database.logdataDao())
//                    }
//                 }
            }
        }

        /**
         * Populate the database in a new coroutine.
         * If you want to start with more words, just add them.
         */
        suspend fun populateDatabase(wordDao: LogDataDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
//            wordDao.deleteAllData()

//            var city = City("Sydney")
//            wordDao.insert(city)
//            city = City("Seattle")
//            wordDao.insert(city)
//            city = City("Portland")
//            wordDao.insert(city)
        }
    }
}