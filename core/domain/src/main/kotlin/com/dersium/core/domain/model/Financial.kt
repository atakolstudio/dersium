package com.dersium.core.domain.model

import java.time.LocalDate

data class ExtraIncome(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: LocalDate = LocalDate.now(),
    val category: IncomeCategory = IncomeCategory.OTHER,
    val notes: String = "",
    val seasonId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: LocalDate = LocalDate.now(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val notes: String = "",
    val seasonId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class IncomeCategory(val displayName: String, val icon: String) {
    TUTORING("Özel Ders", "school"),
    CONSULTING("Danışmanlık", "support_agent"),
    OTHER("Diğer", "attach_money");

    companion object {
        fun fromName(name: String) = entries.find { it.name == name } ?: OTHER
    }
}

enum class ExpenseCategory(val displayName: String, val icon: String) {
    BOOKS("Kitap & Materyal", "menu_book"),
    TRANSPORT("Ulaşım", "directions_car"),
    TECHNOLOGY("Teknoloji", "devices"),
    MARKETING("Pazarlama", "campaign"),
    OTHER("Diğer", "shopping_cart");

    companion object {
        fun fromName(name: String) = entries.find { it.name == name } ?: OTHER
    }
}

data class Season(
    val id: Long = 0,
    val name: String,
    val startYear: Int = 2025,
    val endYear: Int = 2026,
    val isActive: Boolean = true,
    // Optional convenience fields — not stored in DB
    val startDate: java.time.LocalDate = java.time.LocalDate.of(startYear, 9, 1),
    val endDate: java.time.LocalDate = java.time.LocalDate.of(endYear, 6, 30),
) {
    val displayName: String get() = "$startYear-$endYear"
}

data class FinancialSummary(
    val totalLessonIncome: Double = 0.0,
    val totalExtraIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val totalLessons: Int = 0,
    val paidLessons: Int = 0,
    val pendingLessons: Int = 0,
) {
    val totalIncome: Double get() = totalLessonIncome + totalExtraIncome
    val netIncome: Double get() = totalIncome - totalExpenses
    val collectionRate: Double
        get() = if (totalLessonIncome > 0) (paidAmount / totalLessonIncome * 100) else 100.0
}
