package com.dersium.core.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Sends a message via WhatsApp when a phone number is available (deep-links straight into
 * a chat with the text pre-filled via the wa.me web intent — no extra permission needed,
 * works whether or not the number is already a contact). Falls back to the system share
 * sheet — so the user can still pick SMS, another messenger, copy it, etc. — when there's
 * no phone number on file, or WhatsApp isn't installed.
 */
fun Context.shareViaWhatsAppOrSheet(phone: String?, message: String) {
    val normalized = phone?.filter { it.isDigit() }?.let { digits ->
        when {
            digits.isEmpty() -> null
            digits.startsWith("90") -> digits
            digits.startsWith("0") -> "90${digits.substring(1)}"
            digits.length == 10 -> "90$digits"
            else -> digits
        }
    }
    if (!normalized.isNullOrBlank()) {
        try {
            val whatsappIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/$normalized?text=${Uri.encode(message)}"),
            )
            startActivity(whatsappIntent)
            return
        } catch (_: ActivityNotFoundException) {
            // fall through to the generic share sheet below
        }
    }
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    startActivity(Intent.createChooser(shareIntent, null))
}
