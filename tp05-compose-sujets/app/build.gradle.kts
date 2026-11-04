plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.formation.compose"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "fr.formation.compose"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        generateLocaleConfig = true   // sujet 15 : langue de l'application dans les réglages Android 13+
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)     // viewModel()
    implementation(libs.androidx.lifecycle.runtime.compose)       // collectAsStateWithLifecycle()
    implementation(libs.androidx.compose.material.icons.core)     // Icons.Filled.*
    implementation(libs.androidx.compose.material3.adaptive)      // currentWindowAdaptiveInfo()
    debugImplementation(libs.androidx.compose.ui.tooling)
}
