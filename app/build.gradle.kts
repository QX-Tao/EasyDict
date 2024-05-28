plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.materialthemebuilder)
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Amber" to "FFC107",
            "Blue" to "2196F3",
            "BlueGrey" to "607D8F",
            "Brown" to "795548",
            "Cyan" to "00BCD4",
            "DeepOrange" to "FF5722",
            "DeepPurple" to "673AB7",
            "Green" to "4FAF50",
            "Indigo" to "3F51B5",
            "LightBlue" to "03A9F4",
            "LightGreen" to "8BC3A4",
            "Lime" to "CDDC39",
            "Orange" to "FF9800",
            "Pink" to "E91E63",
            "Purple" to "9C27B0",
            "Red" to "F44336",
            "Sakura" to "FF9CA8",
            "Teal" to "009688",
            "Yellow" to "FFEB3B"
        )) {
            create("Material$name") {
                lightThemeFormat = "ThemeOverlay.Light.%s"
                darkThemeFormat = "ThemeOverlay.Dark.%s"
                primaryColor = "#$color"
            }
        }
    }
    generatePalette = true
}

android {
    namespace = properties["project.app.packageName"].toString()
    compileSdk = properties["project.android.compileSdk"].toString().toInt()

    defaultConfig {
        applicationId = properties["project.app.packageName"].toString()
        minSdk = properties["project.android.minSdk"].toString().toInt()
        targetSdk = properties["project.android.targetSdk"].toString().toInt()
        versionName = properties["project.app.versionName"].toString()
        versionCode = properties["project.app.versionCode"].toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDir("libs")
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.xhttp2)
    implementation(libs.photoview)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.autofittextview)
    implementation(libs.flexbox)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.webprogress)
    implementation(libs.andratingbar)
    implementation(libs.coil)
    implementation(libs.permissionsdispatcher)
    implementation(libs.roundview)
    implementation(libs.rikkax.core)
    implementation(libs.rikkax.material)
    implementation(libs.rikkax.material.preference)
    kapt(libs.permissionsdispatcher.processor)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
configurations.all {
    exclude("androidx.appcompat", "appcompat")
}