package com.quotecards.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [QuoteEntity::class],
    version = 3,
    exportSchema = false
)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao

    companion object {
        const val DATABASE_NAME = "quote_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quotes ADD COLUMN category TEXT NOT NULL DEFAULT 'All'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Convert preset "All" category to empty string (uncategorized)
                db.execSQL("UPDATE quotes SET category = '' WHERE category = 'All'")
            }
        }
    }
}
