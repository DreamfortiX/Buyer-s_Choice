plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.reviews"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.reviews"
        minSdk = 21
        targetSdk = 36
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Lifecycle (Views)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Android 12+ system splash screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines (used across the app)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Room (SQLite)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // CardView for home UI cards
    implementation("androidx.cardview:cardview:1.0.0")

    // RecyclerView for History list
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Paging 3 for large history lists
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")

    // Glide for image loading (word cloud)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Shimmer for loading placeholders
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Lottie for vector animations
    implementation("com.airbnb.android:lottie:6.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}