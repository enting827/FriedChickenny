plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
//    Firebase
    id("com.google.gms.google-services")
//    Parcelize
    id ("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.example.friedchickenny"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.friedchickenny"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Import the Firebase BoM
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.2") //carousel
    implementation ("com.google.android.material:material:1.4.0") //material design library - bottom naviagtion view / tablayout
    implementation ("com.github.bumptech.glide:glide:4.13.2") //image load from url
    implementation ("com.github.shuhart:stepview:1.5.1") //stepview / progress bar
    implementation ("com.ncorti:slidetoact:0.11.0") // slider
}