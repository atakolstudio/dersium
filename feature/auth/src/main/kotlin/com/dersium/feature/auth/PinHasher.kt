package com.dersium.feature.auth

import java.security.MessageDigest

object PinHasher {
    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, hashedPin: String): Boolean {
        return hash(pin) == hashedPin
    }
}
