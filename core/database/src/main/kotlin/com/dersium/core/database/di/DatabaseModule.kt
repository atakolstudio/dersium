package com.dersium.core.database.di

import android.content.Context
import androidx.room.Room
import com.dersium.core.database.DersiumDatabase
import com.dersium.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DersiumDatabase =
        Room.databaseBuilder(context, DersiumDatabase::class.java, "dersium.db")
            .addMigrations(
                DersiumDatabase.MIGRATION_2_3,
                DersiumDatabase.MIGRATION_3_4,
                DersiumDatabase.MIGRATION_4_5,
            )
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT OR IGNORE INTO seasons (id, name, startYear, endYear, isActive) VALUES (1, '2025-2026', 2025, 2026, 1)")
                }
            })
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideStudentDao(db: DersiumDatabase) = db.studentDao()
    @Provides fun provideLessonDao(db: DersiumDatabase) = db.lessonDao()
    @Provides fun provideFinancialDao(db: DersiumDatabase) = db.financialDao()
    @Provides fun provideSeasonDao(db: DersiumDatabase) = db.seasonDao()
}
