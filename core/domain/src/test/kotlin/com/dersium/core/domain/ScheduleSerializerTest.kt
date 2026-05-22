package com.dersium.core.domain

import com.dersium.core.domain.model.ScheduleSerializer
import com.dersium.core.domain.model.ScheduleSlot
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ScheduleSerializerTest {
    @Test fun `serialize empty returns empty`() = assertEquals("", ScheduleSerializer.serialize(emptyList()))
    @Test fun `serialize single slot`() = assertEquals("MONDAY:09:00:60", ScheduleSerializer.serialize(listOf(ScheduleSlot(DayOfWeek.MONDAY, LocalTime.of(9,0), 60))))
    @Test fun `deserialize empty returns empty`() = assertTrue(ScheduleSerializer.deserialize("").isEmpty())
    @Test fun `deserialize invalid returns empty`() = assertTrue(ScheduleSerializer.deserialize("INVALID").isEmpty())
    @Test fun `serialize then deserialize`() {
        val slots = listOf(ScheduleSlot(DayOfWeek.TUESDAY, LocalTime.of(10,0), 60))
        val result = ScheduleSerializer.deserialize(ScheduleSerializer.serialize(slots))
        assertEquals(1, result.size)
        assertEquals(DayOfWeek.TUESDAY, result[0].dayOfWeek)
    }
}
