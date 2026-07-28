package com.dersium.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dersium.app.MainActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * GlanceAppWidget can't be constructed by Hilt directly (it isn't an Android framework
 * component Hilt knows how to inject into), so it reaches the repositories it needs
 * through an EntryPoint instead — the standard pattern for Hilt + Glance.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DersiumWidgetEntryPoint {
    fun lessonRepository(): com.dersium.core.domain.repository.LessonRepository
    fun financialRepository(): com.dersium.core.domain.repository.FinancialRepository
    fun userPreferencesRepository(): com.dersium.core.domain.repository.UserPreferencesRepository
}

/** Home screen widget: today's lesson count + this season's pending payment total. */
class DersiumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DersiumWidgetEntryPoint::class.java,
        )
        val prefs = entryPoint.userPreferencesRepository().userPreferences.first()
        val seasonId = prefs.activeSeasonId
        val todayLessonCount = entryPoint.lessonRepository().getLessonsByDate(LocalDate.now()).first()
            .count { it.seasonId == seasonId }
        val pendingAmount = entryPoint.financialRepository().getFinancialSummary(seasonId).first().pendingAmount

        provideContent {
            WidgetContent(todayLessonCount = todayLessonCount, pendingAmount = pendingAmount, currency = prefs.currency)
        }
    }
}

@Composable
private fun WidgetContent(todayLessonCount: Int, pendingAmount: Double, currency: String) {
    val context = androidx.glance.LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(day = Color(0xFF13131A), night = Color(0xFF13131A)))
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Text(
            text = "Dersium",
            style = TextStyle(fontSize = 12.sp, color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8))),
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = if (todayLessonCount > 0) "Bugün $todayLessonCount ders" else "Bugün ders yok",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorProvider(day = Color(0xFFF1F5F9), night = Color(0xFFF1F5F9))),
        )
        if (pendingAmount > 0) {
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "Bekleyen: ${pendingAmount.toInt()} $currency",
                style = TextStyle(fontSize = 14.sp, color = ColorProvider(day = Color(0xFFF59E0B), night = Color(0xFFF59E0B))),
            )
        }
    }
}
