package com.inspirecoding.supershopper.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Category::class], version = 1
)
abstract class ShopperDatabase : RoomDatabase() {

    abstract fun shopperDao(): ShopperDao

    class Callback @Inject constructor(
        private val database: Provider<ShopperDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().shopperDao()



        }

    }


}