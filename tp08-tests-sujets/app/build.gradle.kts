plugins {
    alias(libs.plugins.android.application)   // AGP 9+ : Kotlin intégré, plus de plugin kotlin.android
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.formation.testsujets"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "fr.formation.testsujets"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.androidx.compose.material.icons.core)

    // MVVM : viewModel() dans Compose, collectAsStateWithLifecycle()
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)


    // ---------- Tests unitaires (JVM) : src/test/ ----------
    testImplementation(libs.junit)                       // étapes 0 et 1
    testImplementation(libs.kotlinx.coroutines.test)     // étapes 2 à 5 : runTest, temps virtuel
    testImplementation(libs.mockk)                       // étapes 2 et 4 : mocks
    testImplementation(libs.turbine)                     // étape 5 : tester un Flow

    // ---------- Tests instrumentés (émulateur) : src/androidTest/ ----------
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)   // étape 6 : createComposeRule
    debugImplementation(libs.androidx.compose.ui.test.manifest)       // l'Activity vide de createComposeRule

    debugImplementation(libs.androidx.compose.ui.tooling)
}
