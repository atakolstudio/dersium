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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfReportGenerator {

    private const val PAGE_WIDTH  = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN      = 40f
    private const val LINE_HEIGHT = 20f

    fun generateStudentReport(
        context: Context,
        student: Student,
        lessons: List<Lesson>,
        currency: String = "₺",
    ): File {
        val document = PdfDocument()
        var pageNum = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        var canvas = page.canvas
        var y = 80f

        val titlePaint   = Paint().apply { textSize = 22f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#6366F1") }
        val headingPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; color = android.graphics.Color.BLACK }
        val bodyPaint    = Paint().apply { textSize = 11f; color = android.graphics.Color.DKGRAY }
        val smallPaint   = Paint().apply { textSize = 9f; color = android.graphics.Color.GRAY }
        val linePaint    = Paint().apply { color = android.graphics.Color.parseColor("#E0E0E0"); strokeWidth = 1f }

        fun newPage() {
            document.finishPage(page); pageNum++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
            canvas = page.canvas; y = MARGIN + 20f
        }
        fun checkPage() { if (y > PAGE_HEIGHT - MARGIN - 40) newPage() }
        fun line() { canvas.drawLine(MARGIN, y, (PAGE_WIDTH - MARGIN), y, linePaint); y += 8f }
        fun text(t: String, p: Paint) { checkPage(); canvas.drawText(t, MARGIN, y, p); y += LINE_HEIGHT }

        // Header
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 60f, Paint().apply { color = android.graphics.Color.parseColor("#0A0A1F") })
        canvas.drawText("DERSIUM", MARGIN, 38f, Paint().apply { textSize = 24f; isFakeBoldText = true; color = android.graphics.Color.WHITE })
        canvas.drawText("Ogrenci Raporu", MARGIN + 180f, 38f, Paint().apply { textSize = 12f; color = android.graphics.Color.parseColor("#A0A0C0") })
        y = 80f

        text("${student.fullName} - Ders Raporu", titlePaint)
        text("Tarih: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}", smallPaint)
        line()

        text("OGRENCI BILGILERI", headingPaint)
        if (student.school.isNotEmpty()) text("Okul: ${student.school} - ${student.grade}", bodyPaint)
        if (student.parentName.isNotEmpty()) text("Veli: ${student.parentName}  Tel: ${student.parentPhone}", bodyPaint)
        text("Ders Ucreti: $currency${student.lessonFee.toInt()}/ders  |  ${student.paymentType.displayName}", bodyPaint)
        y += 8f; line()

        val paid    = lessons.filter { it.isPaid }
        val pending = lessons.filter { !it.isPaid }
        text("ISTATISTIKLER", headingPaint)
        text("Toplam: ${lessons.size} ders  |  Odenen: ${paid.size}  |  Bekleyen: ${pending.size}", bodyPaint)
        text("Tahsil Edilen: $currency${paid.sumOf{it.fee}.toInt()}  |  Bekleyen: $currency${pending.sumOf{it.fee}.toInt()}", bodyPaint)
        text("Toplam Sure: ${lessons.sumOf{it.durationMinutes}/60}s ${lessons.sumOf{it.durationMinutes}%60}dk", bodyPaint)
        y += 8f; line()

        text("DERS DETAYLARI", headingPaint)
        y += 4f
        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val hdr = Paint().apply { textSize = 10f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#6366F1") }
        canvas.drawText("Tarih", MARGIN, y, hdr)
        canvas.drawText("Sure", MARGIN+100f, y, hdr)
        canvas.drawText("Konu", MARGIN+170f, y, hdr)
        canvas.drawText("Ucret", MARGIN+360f, y, hdr)
        canvas.drawText("Durum", MARGIN+430f, y, hdr)
        y += 6f; line()

        lessons.sortedByDescending { it.date }.forEach { l ->
            checkPage()
            val rp = Paint().apply { textSize = 10f; color = android.graphics.Color.DKGRAY }
            val sp = Paint().apply { textSize = 10f; color = if (l.isPaid) android.graphics.Color.parseColor("#22C55E") else android.graphics.Color.parseColor("#F59E0B") }
            canvas.drawText(l.date.format(fmt), MARGIN, y, rp)
            canvas.drawText("${l.durationMinutes}dk", MARGIN+100f, y, rp)
            canvas.drawText(l.topic.take(22), MARGIN+170f, y, rp)
            canvas.drawText("$currency${l.fee.toInt()}", MARGIN+360f, y, rp)
            canvas.drawText(if (l.isPaid) "Odendi" else "Bekleyen", MARGIN+430f, y, sp)
            y += LINE_HEIGHT
        }

        document.finishPage(page)
        val f = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir,
            "Dersium_${student.fullName.replace(" ","_")}_${System.currentTimeMillis()}.pdf")
        f.parentFile?.mkdirs()
        document.writeTo(FileOutputStream(f))
        document.close()
        return f
    }

    fun generateSeasonReport(
        context: Context,
        season: Season,
        lessons: List<Lesson>,
        students: List<Student>,
        currency: String = "₺",
    ): File {
        val document = PdfDocument()
        var pageNum = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        var canvas = page.canvas
        var y = 80f

        val titlePaint   = Paint().apply { textSize = 20f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#6366F1") }
        val headingPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; color = android.graphics.Color.BLACK }
        val bodyPaint    = Paint().apply { textSize = 11f; color = android.graphics.Color.DKGRAY }
        val linePaint    = Paint().apply { color = android.graphics.Color.parseColor("#E0E0E0"); strokeWidth = 1f }

        fun newPage() {
            document.finishPage(page); pageNum++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
            canvas = page.canvas; y = MARGIN + 20f
        }
        fun checkPage() { if (y > PAGE_HEIGHT - MARGIN - 40) newPage() }
        fun line() { canvas.drawLine(MARGIN, y, (PAGE_WIDTH-MARGIN), y, linePaint); y += 8f }
        fun text(t: String, p: Paint) { checkPage(); canvas.drawText(t, MARGIN, y, p); y += LINE_HEIGHT }

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 60f, Paint().apply { color = android.graphics.Color.parseColor("#0A0A1F") })
        canvas.drawText("DERSIUM", MARGIN, 38f, Paint().apply { textSize = 24f; isFakeBoldText = true; color = android.graphics.Color.WHITE })
        canvas.drawText("Sezon Raporu", MARGIN+180f, 38f, Paint().apply { textSize = 12f; color = android.graphics.Color.parseColor("#A0A0C0") })
        y = 80f

        text("Sezon Raporu: ${season.displayName}", titlePaint)
        line()

        val paid    = lessons.filter { it.isPaid }
        val pending = lessons.filter { !it.isPaid }
        val rate    = if (lessons.isNotEmpty()) (paid.size * 100 / lessons.size) else 0

        text("SEZON OZETI", headingPaint)
        text("Ogrenci: ${students.size}  |  Toplam Ders: ${lessons.size}", bodyPaint)
        text("Odenen: ${paid.size}  |  Bekleyen: ${pending.size}  |  Tahsilat: %$rate", bodyPaint)
        text("Toplam Gelir: $currency${(paid.sumOf{it.fee}+pending.sumOf{it.fee}).toInt()}", bodyPaint)
        text("Tahsil Edilen: $currency${paid.sumOf{it.fee}.toInt()}  |  Bekleyen: $currency${pending.sumOf{it.fee}.toInt()}", bodyPaint)
        y += 8f; line()

        text("OGRENCI BAZLI OZET", headingPaint)
        y += 4f
        students.forEach { s ->
            checkPage()
            val sl = lessons.filter { it.studentId == s.id }
            val sp = sl.filter { it.isPaid }.sumOf { it.fee }
            val su = sl.filter { !it.isPaid }.sumOf { it.fee }
            val np = Paint().apply { textSize = 11f; isFakeBoldText = true; color = android.graphics.Color.BLACK }
            val gp = Paint().apply { textSize = 10f; color = android.graphics.Color.parseColor("#22C55E") }
            val pp = Paint().apply { textSize = 10f; color = android.graphics.Color.parseColor("#F59E0B") }
            canvas.drawText(s.fullName, MARGIN, y, np)
            canvas.drawText("${sl.size} ders", MARGIN+200f, y, bodyPaint)
            canvas.drawText("$currency${sp.toInt()}", MARGIN+280f, y, gp)
            canvas.drawText("Bkl: $currency${su.toInt()}", MARGIN+370f, y, pp)
            y += LINE_HEIGHT
        }

        document.finishPage(page)
        val f = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir,
            "Dersium_Sezon_${season.displayName}_${System.currentTimeMillis()}.pdf")
        f.parentFile?.mkdirs()
        document.writeTo(FileOutputStream(f))
        document.close()
        return f
    }
}
