// AVANT MIGRATION : vues XML + View Binding (pas de Compose)
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "fr.formation.parametres"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "fr.formation.parametres"
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
        // Génère ActivityParametresBinding à partir de activity_parametres.xml
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)                 // AppCompatActivity
    implementation(libs.material)                           // MaterialSwitch, thèmes Material 3 XML
    implementation(libs.androidx.activity.ktx)              // by viewModels()
    implementation(libs.androidx.lifecycle.runtime.ktx)     // lifecycleScope, repeatOnLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
