package com.dersium.core.database.entity

import androidx.room.*

@Entity(tableName = "seasons")
data class SeasonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startYear: Int,
    val endYear: Int,
    val isActive: Boolean = true,
)

@Entity(tableName = "students", indices = [Index("seasonId")])
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val surname: String = "",
    val avatarColor: String = "#6366F1",
    val lessonFee: Double,
    val paymentType: String = "UPFRONT",
    val lessonCountForPayment: Int = 1,
    val school: String = "",
    val grade: String = "",
    val motherName: String = "",
    val motherPhone: String = "",
    val fatherName: String = "",
    val fatherPhone: String = "",
    val phone: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val seasonId: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduleSlots: String = "",
)

@Entity(tableName = "lessons", indices = [Index("studentId"), Index("date")])
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: Long,
    val startTime: Int,
    val durationMinutes: Int = 60,
    val fee: Double,
    val topic: String = "",
    val notes: String = "",
    val paymentStatus: String = "PENDING",
    val seasonId: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "extra_incomes", indices = [Index("seasonId")])
data class ExtraIncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val seasonId: Long = 1L,
)

@Entity(tableName = "expenses", indices = [Index("seasonId")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val seasonId: Long = 1L,
)
