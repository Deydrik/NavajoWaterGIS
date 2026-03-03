package com.example.navajowatergis.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.navajowatergis.data.dao.WellDao
import com.example.navajowatergis.data.entities.WellEntity
import com.example.navajowatergis.data.entities.WellAnalyteEntity

@Database(entities = [WellEntity::class, WellAnalyteEntity::class], version = 2, exportSchema = false)
abstract class WellDatabase : RoomDatabase() {

    abstract fun wellDao(): WellDao

    companion object {
        @Volatile
        private var INSTANCE: WellDatabase? = null

        // Migration from version 1 to 2: create analytes table if it does not exist.
        // This migration is idempotent; if the table already exists, the SQL will fail silently on some DB engines,
        // but Room will execute it and if it throws, you will see an error. If you anticipate many schema states,
        // consider more robust migration logic or destructive migration for dev.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create table for analytes if not present
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `well_analytes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `wellId` INTEGER NOT NULL,
                        `analyteName` TEXT NOT NULL,
                        `concentration` REAL,
                        `dateSampled` TEXT,
                        FOREIGN KEY(`wellId`) REFERENCES `wells`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_well_analytes_wellId` ON `well_analytes` (`wellId`)")
            }
        }

        fun getInstance(context: Context): WellDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WellDatabase::class.java,
                    "navajo_wells.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // fallbackToDestructiveMigration() removed deliberately; migration above handles analytes table.
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
