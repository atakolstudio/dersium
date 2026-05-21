plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace   = "com.dersium.app"
    compileSdk  = 36

    defaultConfig {
        applicationId = "com.dersium.app"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("debugSigned") {
            storeFile     = file("debug.keystore")
            storePassword = "android"
            keyAlias      = "androiddebugkey"
            keyPassword   = "android"
        }
        create("release") {
            storeFile     = file("release.keystore")
            storePassword = "DersiumStore2026!"
            keyAlias      = "dersium"
            keyPassword   = "DersiumKey2026!"
        }
    }

    buildTypes {
        debug {
            signingConfig      = signingConfigs.getByName("debugSigned")
            isDebuggable        = true
        }
        release {
            signingConfig      = signingConfigs.getByName("release")
            isMinifyEnabled    = true
            isShrinkResources  = true
            isDebuggable       = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility            = JavaVersion.VERSION_17
        targetCompatibility            = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.ui)
    implementation(projects.core.network)
    implementation(projects.feature.home)
    implementation(projects.feature.students)
    implementation(projects.feature.lessons)
    implementation(projects.feature.calendar)
    implementation(projects.feature.financial)
    implementation(projects.feature.reports)
    implementation(projects.feature.auth)
    implementation(projects.feature.settings)
    implementation(projects.feature.export)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.startup)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    implementation(libs.bundles.lifecycle)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.workmanager)
    ksp(libs.hilt.workmanager.compiler)
    implementation(libs.coroutines.android)
    implementation(libs.biometric)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
