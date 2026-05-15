# 📚 Dersium — Özel Ders Yönetim Uygulaması

<p align="center">
  <img src="docs/banner.png" alt="Dersium Banner" width="600"/>
</p>

> **Özel ders öğretmenleri için geliştirilmiş, modern Android uygulaması.**  
> Öğrenci takibi, ders programı, ödeme yönetimi ve finansal raporlama tek çatı altında.

---

## 📱 Ekran Görüntüleri

| Ana Sayfa | Öğrenciler | Dersler | Raporlar |
|---|---|---|---|
| ![Home](docs/home.png) | ![Students](docs/students.png) | ![Lessons](docs/lessons.png) | ![Reports](docs/reports.png) |

---

## 🏗 Mimari

```
Dersium/
├── app/                        # Uygulama modülü (DI wiring, Navigation)
├── build-logic/                # Convention plugins (Gradle 9.4.1)
│   └── convention/
├── core/
│   ├── common/                 # Result, extension functions
│   ├── data/                   # Repository impls, DataStore, WorkManager
│   ├── database/               # Room entities, DAOs, database
│   ├── domain/                 # Models, repository interfaces
│   ├── network/                # Retrofit + OkHttp + Moshi
│   └── ui/                     # Compose theme, shared components
└── feature/
    ├── auth/                   # PIN + Biometric giriş
    ├── home/                   # Ana panel
    ├── students/               # Öğrenci yönetimi
    ├── lessons/                # Ders takibi
    ├── calendar/               # Haftalık takvim
    ├── financial/              # Gelir/Gider yönetimi
    ├── reports/                # Analitik raporlar
    └── settings/               # Ayarlar (PIN, tema, sezon)
```

**Desen:** Clean Architecture + MVVM  
**DI:** Hilt 2.55  
**Database:** Room 2.7.1 (KSP)  
**Async:** Kotlin Coroutines + Flow  
**UI:** Jetpack Compose + Material 3

---

## ⚙️ Teknik Gereksinimler

| Bileşen | Versiyon |
|---|---|
| Android SDK | **36** (targetSdk) |
| minSdk | **26** (Android 8.0) |
| Gradle | **9.4.1** |
| AGP | **9.0.0-beta01** |
| Kotlin | **2.1.21** |
| JDK | **17+** |

---

## 🚀 Kurulum

### 1. Klonla

```bash
git clone https://github.com/KULLANICI_ADI/dersium.git
cd dersium
```

### 2. Android SDK Ayarla

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

### 3. Debug APK Derle

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 4. Release APK (imzalı)

```bash
# keystore.properties oluştur:
cat > keystore.properties << EOF
storeFile=../keystore/dersium.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=dersium
keyPassword=YOUR_KEY_PASSWORD
EOF

./gradlew assembleRelease
```

### 5. Otomatik Kurulum (hepsini bir arada)

```bash
chmod +x setup_and_push.sh
./setup_and_push.sh
```

---

## 🧪 Test

```bash
# Unit tests
./gradlew test

# Instrumented tests (emülatör gerekli)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

---

## 🎨 Özellikler

- 🔐 **PIN + Biyometrik** giriş (Face ID / Fingerprint)
- 👨‍🎓 **Öğrenci yönetimi** — avatarlar, sezon bazlı takip, free/premium limit
- 📅 **Ders takibi** — tarih/saat, konu, süre, ödeme durumu
- 📆 **Haftalık takvim** — gün bazlı program görünümü
- 💰 **Finansal yönetim** — Ek gelir + Gider takibi, renk kodlu
- 📊 **8 rapor tipi** — Öğrenci, Ortalama, Aylık, Aktif, Ödeme, Bekleyen, Günlük, Sezon
- 🌙 **Full dark mode** — Indigo/Yeşil/Kırmızı accent palette
- 🌍 **Türkçe** arayüz

---

## 📄 Lisans

MIT © 2026 Dersium
