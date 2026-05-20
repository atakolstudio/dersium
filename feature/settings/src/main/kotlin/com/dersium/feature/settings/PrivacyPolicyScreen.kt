package com.dersium.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dersium.core.ui.theme.DersiumColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DersiumColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Gizlilik Politikası", color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DersiumColors.TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DersiumColors.Background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PolicySection("Son Güncelleme", "20 Mayıs 2026")

            PolicySection("Giriş",
                "Dersium uygulaması (\"Uygulama\"), özel ders veren öğretmenler için geliştirilmiş bir yönetim aracıdır. Bu gizlilik politikası, uygulamamızın hangi verileri topladığını ve bu verileri nasıl kullandığını açıklamaktadır."
            )

            PolicySection("Toplanan Veriler",
                "Dersium, aşağıdaki verileri yalnızca cihazınızda yerel olarak saklar:\n\n" +
                "• Öğrenci bilgileri (isim, okul, veli bilgileri)\n" +
                "• Ders kayıtları (tarih, süre, ücret, konu)\n" +
                "• Finansal veriler (gelir, gider kayıtları)\n" +
                "• Uygulama tercihleri (tema rengi, PIN ayarı)\n\n" +
                "Bu veriler hiçbir sunucuya gönderilmez."
            )

            PolicySection("Veri Saklama",
                "Tüm veriler cihazınızdaki SQLite veritabanında şifresiz olarak saklanır. " +
                "Uygulamayı kaldırdığınızda tüm veriler silinir. " +
                "Yedekleme özelliği ile verilerinizi cihazınızdaki Downloads/Dersium klasörüne kaydedebilirsiniz."
            )

            PolicySection("Üçüncü Taraf Hizmetler",
                "Uygulama, Firebase Crashlytics kullanmaktadır. " +
                "Bu hizmet, uygulama çökmelerini tespit etmek için anonim hata raporları toplar. " +
                "Toplanan veriler: cihaz modeli, Android sürümü, hata yığını. " +
                "Kişisel veriler toplanmaz.\n\n" +
                "Firebase Gizlilik Politikası: https://firebase.google.com/support/privacy"
            )

            PolicySection("WhatsApp Entegrasyonu",
                "Uygulama, ödeme hatırlatmaları için WhatsApp'a yönlendirme yapabilir. " +
                "Bu özellik, cihazınızdaki WhatsApp uygulamasını açar. " +
                "Uygulamamız WhatsApp üzerinden gönderilen mesajları görmez veya saklamaz."
            )

            PolicySection("İzinler",
                "Uygulama aşağıdaki izinleri kullanır:\n\n" +
                "• Depolama: Yedek dosyaları ve PDF raporları kaydetmek için\n" +
                "• Biyometrik: PIN kilidi için (opsiyonel)\n" +
                "• İnternet: Firebase Crashlytics için"
            )

            PolicySection("Çocuk Gizliliği",
                "Uygulama, 13 yaşın altındaki kişilerden bilerek veri toplamaz. " +
                "Öğrenci bilgileri yalnızca öğretmenin cihazında saklanır."
            )

            PolicySection("Değişiklikler",
                "Bu gizlilik politikası güncellenebilir. " +
                "Önemli değişiklikler uygulama içinde bildirilecektir."
            )

            PolicySection("İletişim",
                "Gizlilik politikamız hakkında sorularınız için:\n" +
                "E-posta: support@dersium.app"
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = DersiumColors.Primary, fontWeight = FontWeight.Bold)
        Text(content, style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextSecondary)
        HorizontalDivider(color = DersiumColors.Outline)
    }
}
