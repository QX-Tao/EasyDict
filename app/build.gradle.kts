plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.materialthemebuilder)
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "DodgerBlue" to "1E90FF",
            "SakuraPink" to "FF9CA8",
            "AmberGold" to "DD9C00",
            "PeruBrown" to "795548",
            "VioletPurple" to "673AB7",
            "TealGreen" to "009688",
            "HawthornRed" to "F44336",
            "LimeGreen" to "CDDC39",
            "SkyBlue" to "87CEEB"
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
            isMinifyEnabled = false
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
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.transition.ktx)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.xhttp2)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.flexbox)
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.coil)
    implementation(libs.rikkax.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
configurations.all {
    exclude("androidx.appcompat", "appcompat")
}