package com.dersium.feature.students

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Prefix so a scanned code can be recognized as ours (and not e.g. a random web QR). */
const val STUDENT_QR_PREFIX = "dersium:student:"

fun studentQrContent(studentId: Long): String = "$STUDENT_QR_PREFIX$studentId"

/** Parses a scanned raw value back into a student id, or null if it isn't one of ours. */
fun parseStudentQr(rawValue: String?): Long? =
    rawValue?.takeIf { it.startsWith(STUDENT_QR_PREFIX) }
        ?.removePrefix(STUDENT_QR_PREFIX)
        ?.toLongOrNull()

/** Renders [content] as a black-on-white QR bitmap, [sizePx] square. */
fun generateQrCodeBitmap(content: String, sizePx: Int = 512): ImageBitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap.asImageBitmap()
}
