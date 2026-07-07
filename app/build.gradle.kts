
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
        versionCode = 4
        versionName = "3.0.0"
        
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
            isShrinkResources = true
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
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.github.stuxuhai:jpinyin:1.1.7")
    implementation("com.belerweb:pinyin4j:2.5.0")

//    // TinyPinyin 核心包，约 80KB，必备
//    implementation("com.github.promeg:tinypinyin:2.0.3")
//// 可选：包含中国城市名的词典，能提高“重庆”这类地名的多音字识别率
//    implementation("com.github.promeg:tinypinyin-lexicons-android-cncity:2.0.3")
}
// app/build.gradle.kts (模块级)
