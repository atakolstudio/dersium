package com.dersium.feature.auth

import org.junit.Assert.*
import org.junit.Test

class PinHasherTest {
    @Test fun `hash returns 64 char SHA-256`() = assertEquals(64, PinHasher.hash("1234").length)
    @Test fun `same pin same hash`() = assertEquals(PinHasher.hash("1234"), PinHasher.hash("1234"))
    @Test fun `different pins different hashes`() = assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("5678"))
    @Test fun `verify correct pin`() = assertTrue(PinHasher.verify("1234", PinHasher.hash("1234")))
    @Test fun `verify wrong pin`() = assertFalse(PinHasher.verify("9999", PinHasher.hash("1234")))
}
