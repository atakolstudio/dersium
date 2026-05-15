package com.dersium.core.data.di

import com.dersium.core.data.repository.FinancialRepositoryImpl
import com.dersium.core.data.repository.LessonRepositoryImpl
import com.dersium.core.data.repository.StudentRepositoryImpl
import com.dersium.core.data.repository.UserPreferencesRepositoryImpl
import com.dersium.core.domain.repository.FinancialRepository
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.StudentRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    @Binds
    @Singleton
    abstract fun bindFinancialRepository(impl: FinancialRepositoryImpl): FinancialRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
