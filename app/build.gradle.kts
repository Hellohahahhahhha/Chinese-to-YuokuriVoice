
plugins {
    id("com.android.application")
    
}

android {
    namespace = "com.example.chinesetoy"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "com.example.chinesetoy"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        // dataBinging = true
    }
    
}

dependencies {


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.8.9") // Kotlin DSL 语法
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
}
// app/build.gradle.kts (模块级)
