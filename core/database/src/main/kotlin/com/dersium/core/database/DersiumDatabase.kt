package com.dersium.core.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dersium.core.database.dao.*
import com.dersium.core.database.entity.*

@Database(
    entities = [
        SeasonEntity::class,
        StudentEntity::class,
        LessonEntity::class,
        ExtraIncomeEntity::class,
        ExpenseEntity::class,
    ],
    views = [LessonWithStudentView::class],
    version = 3,
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
    }
}
