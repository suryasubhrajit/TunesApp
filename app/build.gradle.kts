plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.shaadow.tunes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shaadow.tunes"
        minSdk = 21
        targetSdk = 35
        versionCode = 6
        versionName = "0.6"
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
        jvmTarget = "17"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.coil)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.core.splashscreen)
    implementation(libs.exoplayer)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.reorderable)
    implementation(libs.room)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.inappmessaging.display)
    ksp(libs.room.compiler)
    implementation(projects.innertube)
    implementation(projects.kugou)
    coreLibraryDesugaring(libs.desugaring)
}