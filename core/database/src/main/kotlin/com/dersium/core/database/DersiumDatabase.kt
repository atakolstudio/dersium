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
                try {
                    db.execSQL("UPDATE students SET motherName = parentName, motherPhone = parentPhone")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new students table without parentName/parentPhone, with scheduleSlots
                db.execSQL("""
                    CREATE TABLE students_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        surname TEXT NOT NULL DEFAULT '',
                        avatarColor TEXT NOT NULL DEFAULT '#6366F1',
                        lessonFee REAL NOT NULL,
                        paymentType TEXT NOT NULL DEFAULT 'UPFRONT',
                        lessonCountForPayment INTEGER NOT NULL DEFAULT 1,
                        school TEXT NOT NULL DEFAULT '',
                        grade TEXT NOT NULL DEFAULT '',
                        motherName TEXT NOT NULL DEFAULT '',
                        motherPhone TEXT NOT NULL DEFAULT '',
                        fatherName TEXT NOT NULL DEFAULT '',
                        fatherPhone TEXT NOT NULL DEFAULT '',
                        phone TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        isActive INTEGER NOT NULL DEFAULT 1,
                        seasonId INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        scheduleSlots TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                // Copy data from old table
                db.execSQL("""
                    INSERT INTO students_new (
                        id, name, surname, avatarColor, lessonFee, paymentType,
                        lessonCountForPayment, school, grade,
                        motherName, motherPhone, fatherName, fatherPhone,
                        phone, notes, isActive, seasonId, createdAt, scheduleSlots
                    )
                    SELECT
                        id, name, surname, avatarColor, lessonFee, paymentType,
                        lessonCountForPayment, school, grade,
                        COALESCE(motherName, ''), COALESCE(motherPhone, ''),
                        COALESCE(fatherName, ''), COALESCE(fatherPhone, ''),
                        phone, notes, isActive, seasonId, createdAt,
                        COALESCE(scheduleSlots, '')
                    FROM students
                """.trimIndent())

                db.execSQL("DROP TABLE students")
                db.execSQL("ALTER TABLE students_new RENAME TO students")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_students_seasonId ON students(seasonId)")
            }
        }
    }
}
