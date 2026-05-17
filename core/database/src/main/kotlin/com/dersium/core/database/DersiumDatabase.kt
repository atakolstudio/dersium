package com.dersium.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dersium.core.database.dao.*
import com.dersium.core.database.entity.*

@Database(
    entities = [SeasonEntity::class, StudentEntity::class, LessonEntity::class, ExtraIncomeEntity::class, ExpenseEntity::class],
    views = [LessonWithStudentView::class],
    version = 5,
    exportSchema = true,
)
abstract class DersiumDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun lessonDao(): LessonDao
    abstract fun financialDao(): FinancialDao
    abstract fun seasonDao(): SeasonDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN lessonCountForPayment INTEGER NOT NULL DEFAULT 1")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN motherName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN motherPhone TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN fatherName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN fatherPhone TEXT NOT NULL DEFAULT ''")
                try { db.execSQL("UPDATE students SET motherName = parentName, motherPhone = parentPhone") } catch (_: Exception) {}
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN scheduleSlots TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
