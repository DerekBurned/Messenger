plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    // Realm Kotlin plugin is disabled: realm-kotlin 3.0.0 is incompatible with Kotlin 2.3.10
    // (compiler API NoSuchMethodError on FirResolvedTypeRef.getType()). Re-enable once Realm
    // ships a Kotlin 2.3-compatible release, then complete Phase 2 of the integration plan.
    // alias(libs.plugins.realm.kotlin)
}

android {
    namespace = "com.example.messenger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.messenger"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin{
        target{
            compilerOptions {
                optIn.add("kotlin.RequiresOptIn")
            }
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Force matching concurrent-futures across all configurations to resolve a strict-version
// conflict between WorkManager (pulls 1.1.0) and androidx.test 1.3.0 / espresso 3.7.0 (need 1.2.0).
configurations.all {
    resolutionStrategy {
        force("androidx.concurrent:concurrent-futures:1.2.0")
        force("androidx.concurrent:concurrent-futures-ktx:1.2.0")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.text.android)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)


    // Firebase - BOM manages all versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.database)
    implementation(libs.google.gms.auth.phone)

    // Room (kept during Room→Realm transition; Realm is primary cache per tech spec)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Realm Kotlin SDK is declared in libs.versions.toml but not wired here — blocked by
    // Kotlin 2.3 incompatibility; see plugins block comment.
    // implementation(libs.realm.base)
    implementation(libs.androidx.compose.foundation)

    // Hilt - using KSP
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)

    // Navigation (Nav2 kept during transition; Nav3 primary per tech spec)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Gson
    implementation(libs.google.gson)

    //AgoraRTC
    implementation("io.agora.rtc:voice-sdk:4.5.0")
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}