package com.dersium.feature.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.Season
import com.dersium.core.domain.model.Student
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object PdfReportGenerator {
    private const val W = 595; private const val H = 842; private const val M = 36f; private const val LH = 18f
    private val C_BG = android.graphics.Color.parseColor("#0D0D1A")
    private val C_PRIMARY = android.graphics.Color.parseColor("#6366F1")
    private val C_GREEN = android.graphics.Color.parseColor("#22C55E")
    private val C_YELLOW = android.graphics.Color.parseColor("#F59E0B")
    private val C_RED = android.graphics.Color.parseColor("#EF4444")
    private val C_GRAY = android.graphics.Color.parseColor("#94A3B8")
    private val C_LINE = android.graphics.Color.parseColor("#E2E8F0")
    private val C_BLACK = android.graphics.Color.parseColor("#1E293B")
    private val C_WHITE = android.graphics.Color.WHITE

    private fun p(size: Float, color: Int, bold: Boolean = false) = Paint().apply { textSize = size; this.color = color; isFakeBoldText = bold; isAntiAlias = true }
    private fun rp(color: Int) = Paint().apply { this.color = color; style = Paint.Style.FILL; isAntiAlias = true }
    private fun lp(color: Int, w: Float = 1f) = Paint().apply { this.color = color; strokeWidth = w; isAntiAlias = true }

    private class Pg(val doc: PdfDocument, var num: Int = 1) {
        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, num).create())!!
        var c = page.canvas!!
        var y = M + 80f

        fun np() {
            doc.finishPage(page); num++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, num).create())!!
            c = page.canvas!!; y = M + 20f
        }
        fun chk(n: Float = LH + 4f) { if (y + n > H - M) np() }
        fun txt(t: String, pt: Paint, x: Float = M) { chk(); c.drawText(t, x, y, pt); y += LH }
        fun ln(color: Int = C_LINE, w: Float = 1f) { chk(6f); c.drawLine(M, y, (W - M), y, lp(color, w)); y += 6f }
        fun sp(h: Float = 8f) { y += h }
        fun sec(title: String) {
            chk(26f); sp(6f)
            c.drawRect(M, y - 14f, W - M, y + 4f, rp(C_PRIMARY))
            c.drawText(title, M + 8f, y, p(10f, C_WHITE, true))
            y += 14f; sp(2f)
        }
        fun row(l: String, r: String, lc: Int = C_BLACK, rc: Int = C_BLACK) {
            chk(); c.drawText(l, M + 4f, y, p(10f, lc)); c.drawText(r, W - M - 4f - p(10f, rc).measureText(r), y, p(10f, rc)); y += LH
        }
        fun finish() { doc.finishPage(page) }
    }

    fun generateSeasonReport(context: Context, season: Season, lessons: List<Lesson>, students: List<Student>, currency: String = "₺"): File {
        val doc = PdfDocument(); val pg = Pg(doc)
        // Header
        pg.c.drawRect(0f, 0f, W.toFloat(), 70f, rp(C_BG))
        pg.c.drawText("DERSIUM", M, 32f, p(22f, C_WHITE, true))
        pg.c.drawText("Ozel Ders Yonetim Sistemi", M, 50f, p(10f, C_GRAY))
        pg.c.drawText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), W - M - 120f, 32f, p(9f, C_GRAY))
        pg.c.drawRect(0f, 70f, W.toFloat(), 73f, rp(C_PRIMARY))
        pg.y = 88f
        pg.c.drawText("SEZON RAPORU: ${season.displayName}", M, pg.y, p(16f, C_PRIMARY, true)); pg.y += 24f

        val paid = lessons.filter { it.isPaid }; val pend = lessons.filter { !it.isPaid }
        val pAmt = paid.sumOf { it.fee }; val uAmt = pend.sumOf { it.fee }
        val rate = if (pAmt + uAmt > 0) (pAmt / (pAmt + uAmt) * 100).toInt() else 0

        // Summary
        pg.sec("SEZON OZETI")
        pg.txt("Ogrenci: ${students.size}  |  Toplam Ders: ${lessons.size}", p(10f, C_BLACK))
        pg.txt("Odenen: ${paid.size}  |  Bekleyen: ${pend.size}  |  Tahsilat: %$rate", p(10f, C_BLACK))
        pg.txt("Toplam Gelir: $currency${(pAmt+uAmt).toInt()}", p(10f, C_BLACK))
        pg.txt("Tahsil Edilen: $currency${pAmt.toInt()}  |  Bekleyen: $currency${uAmt.toInt()}", p(10f, C_BLACK))
        // Progress bar
        pg.sp(4f)
        val bW = W - 2 * M
        pg.c.drawRect(M, pg.y, M + bW, pg.y + 8f, rp(C_LINE))
        pg.c.drawRect(M, pg.y, M + bW * rate / 100f, pg.y + 8f, rp(if (rate >= 75) C_GREEN else C_YELLOW))
        pg.y += 14f

        // Student breakdown
        pg.sec("OGRENCI BAZLI OZET")
        pg.c.drawRect(M, pg.y - 13f, W - M, pg.y + 4f, rp(C_BLACK))
        listOf("Ogrenci" to M+4f, "Ders" to M+180f, "Sure" to M+230f, "Odenen" to M+300f, "Bekleyen" to M+390f, "Oran" to M+470f).forEach { (h, x) ->
            pg.c.drawText(h, x, pg.y, p(8f, C_WHITE, true))
        }
        pg.y += 8f; pg.ln()
        students.forEach { s ->
            pg.chk()
            val sl = lessons.filter { it.studentId == s.id }
            val sp2 = sl.filter { it.isPaid }.sumOf { it.fee }
            val su2 = sl.filter { !it.isPaid }.sumOf { it.fee }
            val sr = if (sp2+su2 > 0) (sp2/(sp2+su2)*100).toInt() else 0
            val mins = sl.sumOf { it.durationMinutes }
            pg.c.drawText(s.fullName.take(20), M+4f, pg.y, p(9f, C_BLACK, true))
            pg.c.drawText("${sl.size}", M+180f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("${mins/60}s${mins%60}dk", M+230f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("$currency${sp2.toInt()}", M+300f, pg.y, p(9f, C_GREEN))
            pg.c.drawText("$currency${su2.toInt()}", M+390f, pg.y, p(9f, C_YELLOW))
            pg.c.drawText("%$sr", M+470f, pg.y, p(9f, if(sr>=75) C_GREEN else C_RED, true))
            pg.y += LH; pg.ln(C_LINE, 0.5f)
        }

        // Monthly
        pg.sec("AYLIK OZET")
        pg.c.drawRect(M, pg.y - 13f, W - M, pg.y + 4f, rp(C_BLACK))
        listOf("Ay" to M+4f, "Ders" to M+160f, "Sure" to M+220f, "Odenen" to M+300f, "Bekleyen" to M+400f).forEach { (h, x) ->
            pg.c.drawText(h, x, pg.y, p(8f, C_WHITE, true))
        }
        pg.y += 8f; pg.ln()
        val mFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("tr"))
        lessons.groupBy { it.date.withDayOfMonth(1) }.toSortedMap(compareByDescending { it }).forEach { (mon, ls) ->
            pg.chk()
            val mp = ls.filter { it.isPaid }.sumOf { it.fee }
            val mu = ls.filter { !it.isPaid }.sumOf { it.fee }
            val mm = ls.sumOf { it.durationMinutes }
            pg.c.drawText(mon.format(mFmt).take(18), M+4f, pg.y, p(9f, C_BLACK, true))
            pg.c.drawText("${ls.size}", M+160f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("${mm/60}s${mm%60}dk", M+220f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("$currency${mp.toInt()}", M+300f, pg.y, p(9f, C_GREEN))
            pg.c.drawText("$currency${mu.toInt()}", M+400f, pg.y, p(9f, C_YELLOW))
            pg.y += LH; pg.ln(C_LINE, 0.5f)
        }

        // Daily distribution
        pg.sec("GUNLUK DAGILIM")
        val dayList = listOf(DayOfWeek.MONDAY,DayOfWeek.TUESDAY,DayOfWeek.WEDNESDAY,DayOfWeek.THURSDAY,DayOfWeek.FRIDAY,DayOfWeek.SATURDAY,DayOfWeek.SUNDAY)
        val maxL = dayList.maxOf { d -> lessons.count { it.date.dayOfWeek == d } }.coerceAtLeast(1)
        dayList.forEach { dow ->
            pg.chk(16f)
            val ls = lessons.filter { it.date.dayOfWeek == dow }
            val dn = dow.getDisplayName(TextStyle.FULL, Locale("tr"))
            val bl = (W - M - 200f) * ls.size / maxL
            pg.c.drawText(dn.take(10), M+4f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("${ls.size} ders", M+110f, pg.y, p(9f, C_GRAY))
            pg.c.drawRect(M+165f, pg.y-9f, W-M-4f, pg.y-1f, rp(C_LINE))
            if (ls.isNotEmpty()) pg.c.drawRect(M+165f, pg.y-9f, M+165f+bl, pg.y-1f, rp(C_PRIMARY))
            pg.c.drawText("$currency${ls.filter{it.isPaid}.sumOf{it.fee}.toInt()}", W-M-60f, pg.y, p(9f, C_GREEN))
            pg.y += 15f
        }

        // Lesson details
        pg.sec("DERS DETAYLARI")
        pg.c.drawRect(M, pg.y - 13f, W - M, pg.y + 4f, rp(C_BLACK))
        listOf("Tarih" to M+4f, "Ogrenci" to M+75f, "Sure" to M+195f, "Konu" to M+250f, "Ucret" to M+390f, "Durum" to M+450f).forEach { (h, x) ->
            pg.c.drawText(h, x, pg.y, p(8f, C_WHITE, true))
        }
        pg.y += 8f; pg.ln()
        val dFmt = DateTimeFormatter.ofPattern("dd/MM/yy")
        lessons.sortedByDescending { it.date }.forEach { l ->
            pg.chk()
            val sc = if (l.isPaid) C_GREEN else C_YELLOW
            pg.c.drawText(l.date.format(dFmt), M+4f, pg.y, p(8f, C_BLACK))
            pg.c.drawText(l.studentName.take(16), M+75f, pg.y, p(8f, C_BLACK, true))
            pg.c.drawText("${l.durationMinutes}dk", M+195f, pg.y, p(8f, C_BLACK))
            pg.c.drawText(l.topic.take(16), M+250f, pg.y, p(8f, C_BLACK))
            pg.c.drawText("$currency${l.fee.toInt()}", M+390f, pg.y, p(8f, sc))
            pg.c.drawText(if(l.isPaid) "Odendi" else "Bekleyen", M+450f, pg.y, p(8f, sc, true))
            pg.y += LH; pg.ln(C_LINE, 0.3f)
        }

        pg.sp(8f); pg.ln(C_PRIMARY, 2f)
        pg.txt("Bu rapor Dersium uygulamasi tarafindan olusturulmustur.", p(8f, C_GRAY))
        pg.finish()

        val f = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir, "Dersium_Sezon_${season.displayName}_${System.currentTimeMillis()}.pdf")
        f.parentFile?.mkdirs(); doc.writeTo(FileOutputStream(f)); doc.close(); return f
    }

    fun generateStudentReport(context: Context, student: Student, lessons: List<Lesson>, currency: String = "₺"): File {
        val doc = PdfDocument(); val pg = Pg(doc)
        pg.c.drawRect(0f, 0f, W.toFloat(), 70f, rp(C_BG))
        pg.c.drawText("DERSIUM", M, 32f, p(22f, C_WHITE, true))
        pg.c.drawText("Ogrenci Raporu", M, 50f, p(10f, C_GRAY))
        pg.c.drawRect(0f, 70f, W.toFloat(), 73f, rp(C_PRIMARY))
        pg.y = 88f
        pg.c.drawText(student.fullName, M, pg.y, p(16f, C_PRIMARY, true)); pg.y += 24f

        pg.sec("OGRENCI BILGILERI")
        if (student.school.isNotEmpty()) pg.row("Okul / Sinif", "${student.school} - ${student.grade}")
        if (student.parentName.isNotEmpty()) pg.row("Veli", student.parentName)
        if (student.parentPhone.isNotEmpty()) pg.row("Telefon", student.parentPhone)
        pg.row("Ders Ucreti", "$currency${student.lessonFee.toInt()}/ders")
        pg.row("Odeme Tipi", student.paymentType.displayName)

        val paid = lessons.filter { it.isPaid }; val pend = lessons.filter { !it.isPaid }
        val pAmt = paid.sumOf { it.fee }; val uAmt = pend.sumOf { it.fee }
        val rate = if (pAmt+uAmt > 0) (pAmt/(pAmt+uAmt)*100).toInt() else 0
        val mins = lessons.sumOf { it.durationMinutes }

        pg.sec("ISTATISTIKLER")
        pg.txt("Toplam Ders: ${lessons.size}  |  Odenen: ${paid.size}  |  Bekleyen: ${pend.size}", p(10f, C_BLACK))
        pg.txt("Tahsil: $currency${pAmt.toInt()}  |  Bekleyen: $currency${uAmt.toInt()}  |  Tahsilat: %$rate", p(10f, C_BLACK))
        pg.txt("Toplam Sure: ${mins/60}s ${mins%60}dk", p(10f, C_BLACK))
        pg.sp(4f)
        val bW = W - 2 * M
        pg.c.drawRect(M, pg.y, M+bW, pg.y+8f, rp(C_LINE))
        pg.c.drawRect(M, pg.y, M+bW*rate/100f, pg.y+8f, rp(if(rate>=75) C_GREEN else C_YELLOW))
        pg.y += 14f

        pg.sec("DERS DETAYLARI")
        pg.c.drawRect(M, pg.y-13f, W-M, pg.y+4f, rp(C_BLACK))
        listOf("Tarih" to M+4f, "Sure" to M+110f, "Konu" to M+180f, "Ucret" to M+380f, "Durum" to M+445f).forEach { (h, x) ->
            pg.c.drawText(h, x, pg.y, p(8f, C_WHITE, true))
        }
        pg.y += 8f; pg.ln()
        val dFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        lessons.sortedByDescending { it.date }.forEach { l ->
            pg.chk()
            val sc = if (l.isPaid) C_GREEN else C_YELLOW
            pg.c.drawText(l.date.format(dFmt), M+4f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("${l.durationMinutes}dk", M+110f, pg.y, p(9f, C_BLACK))
            pg.c.drawText(l.topic.take(22), M+180f, pg.y, p(9f, C_BLACK))
            pg.c.drawText("$currency${l.fee.toInt()}", M+380f, pg.y, p(9f, sc))
            pg.c.drawText(if(l.isPaid) "Odendi" else "Bekleyen", M+445f, pg.y, p(9f, sc, true))
            pg.y += LH; pg.ln(C_LINE, 0.3f)
        }
        pg.sp(8f); pg.ln(C_PRIMARY, 2f)
        pg.txt("Bu rapor Dersium uygulamasi tarafindan olusturulmustur.", p(8f, C_GRAY))
        pg.finish()

        val f = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir, "Dersium_${student.fullName.replace(" ","_")}_${System.currentTimeMillis()}.pdf")
        f.parentFile?.mkdirs(); doc.writeTo(FileOutputStream(f)); doc.close(); return f
    }
}
