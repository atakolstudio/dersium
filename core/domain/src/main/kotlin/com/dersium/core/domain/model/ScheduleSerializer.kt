package com.dersium.core.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

object ScheduleSerializer {
    // Format: "MONDAY:09:00:60,WEDNESDAY:14:00:90"
    fun serialize(slots: List<ScheduleSlot>): String =
        slots.joinToString(",") { "${it.dayOfWeek.name}:${it.startTime.hour.toString().padStart(2,'0')}:${it.startTime.minute.toString().padStart(2,'0')}:${it.durationMinutes}" }

    fun deserialize(str: String): List<ScheduleSlot> {
        if (str.isBlank()) return emptyList()
        return str.split(",").mapNotNull { part ->
            val tokens = part.trim().split(":")
            if (tokens.size < 4) return@mapNotNull null
            try {
                ScheduleSlot(
                    dayOfWeek = DayOfWeek.valueOf(tokens[0]),
                    startTime = LocalTime.of(tokens[1].toInt(), tokens[2].toInt()),
                    durationMinutes = tokens[3].toInt(),
                )
            } catch (_: Exception) { null }
        }
    }
}
