package com.dersium.core.data.mapper

import com.dersium.core.database.entity.*
import com.dersium.core.domain.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun StudentEntity.toDomain() = Student(
    id = id, name = name, surname = surname,
    avatarColor = avatarColor, lessonFee = lessonFee,
    paymentType = PaymentType.fromName(paymentType),
    lessonCountForPayment = lessonCountForPayment,
    school = school, grade = grade,
    motherName = motherName, motherPhone = motherPhone,
    fatherName = fatherName, fatherPhone = fatherPhone,
    phone = phone, notes = notes, isActive = isActive,
    seasonId = seasonId, createdAt = createdAt,
)

fun Student.toEntity() = StudentEntity(
    id = id, name = name, surname = surname,
    avatarColor = avatarColor, lessonFee = lessonFee,
    paymentType = paymentType.name,
    lessonCountForPayment = lessonCountForPayment,
    school = school, grade = grade,
    motherName = motherName, motherPhone = motherPhone,
    fatherName = fatherName, fatherPhone = fatherPhone,
    phone = phone, notes = notes, isActive = isActive,
    seasonId = seasonId, createdAt = createdAt,
)

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
fun LocalDate.toEpochMilli(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun Int.toLocalTime(): LocalTime = LocalTime.of(this / 60, this % 60)
fun LocalTime.toMinutes(): Int = hour * 60 + minute

fun com.dersium.core.database.entity.LessonWithStudentView.toDomain() = Lesson(
    id = id, studentId = studentId,
    studentName = studentName, studentAvatarColor = studentAvatarColor,
    date = date.toLocalDate(), startTime = startTime.toLocalTime(),
    durationMinutes = durationMinutes, fee = fee,
    topic = topic, notes = notes,
    paymentStatus = PaymentStatus.fromName(paymentStatus),
    seasonId = seasonId,
)

fun Lesson.toEntity() = LessonEntity(
    id = id, studentId = studentId,
    date = date.toEpochMilli(), startTime = startTime.toMinutes(),
    durationMinutes = durationMinutes, fee = fee,
    topic = topic, notes = notes,
    paymentStatus = paymentStatus.name,
    seasonId = seasonId,
)

fun ExtraIncomeEntity.toDomain() = ExtraIncome(
    id = id, title = title, amount = amount, notes = notes,
    date = date.toLocalDate(), seasonId = seasonId,
)
fun ExtraIncome.toEntity() = ExtraIncomeEntity(
    id = id, title = title, amount = amount, notes = notes,
    date = date.toEpochMilli(), seasonId = seasonId,
)

fun ExpenseEntity.toDomain() = Expense(
    id = id, title = title, amount = amount, notes = notes,
    date = date.toLocalDate(), seasonId = seasonId,
)
fun Expense.toEntity() = ExpenseEntity(
    id = id, title = title, amount = amount, notes = notes,
    date = date.toEpochMilli(), seasonId = seasonId,
)

fun SeasonEntity.toDomain() = Season(
    id = id, name = name,
    startYear = startYear, endYear = endYear, isActive = isActive,
)
fun Season.toEntity() = SeasonEntity(
    id = id, name = name,
    startYear = startYear, endYear = endYear, isActive = isActive,
)
