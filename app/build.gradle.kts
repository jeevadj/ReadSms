plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp.plugin)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serializable)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.readsmssample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.readsmssample"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isDebuggable = true
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }


    packaging{
        resources {
            excludes += arrayOf("/META-INF/{AL2.0,LGPL2.1}","META-INF/INDEX.LIST","META-INF/io.netty.versions.properties")
        }
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
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.nav)
    implementation(libs.accompanist)
    implementation(libs.kotlin.serialization)
    ksp(libs.hilt.compiler)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // microsoft onedrive
    implementation(libs.microsoft.graph)
    implementation(libs.azure.identity)
    implementation(libs.microsoft.msal){
        exclude(group = "io.opentelemetry")
        exclude (group="com.microsoft.device.display")
    }

    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)


    //okhttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

}