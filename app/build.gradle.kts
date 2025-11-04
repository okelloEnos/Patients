plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.okellosoftwarez.patients"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.okellosoftwarez.patients"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)   // or libs.room-runtime if you used that naming
    implementation(libs.room.ktx)
    // Annotation processor -> KSP
    ksp(libs.room.compiler)

    // Retrofit + Gson converter + OkHttp logging
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
//    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.10"
//
//
//// AndroidX
//    implementation 'androidx.core:core-ktx:1.12.0'
//    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
//    implementation 'androidx.activity:activity-compose:1.9.3'
//
//
//// Compose
//    implementation "androidx.compose.ui:ui:${compose_version}"
//    implementation "androidx.compose.material:material:${compose_version}"
//    implementation "androidx.compose.ui:ui-tooling-preview:${compose_version}"
//    debugImplementation "androidx.compose.ui:ui-tooling:${compose_version}"
//
//
//// Navigation Compose
//    implementation 'androidx.navigation:navigation-compose:2.7.0'
//    implementation 'androidx.hilt:hilt-navigation-compose:1.0.0'





//// Coil (optional)
//    implementation 'io.coil-kt:coil-compose:2.4.0'


// Testing
//    testImplementation 'junit:junit:4.13.2'
}