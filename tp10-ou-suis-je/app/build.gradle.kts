import java.util.Properties

plugins {
    alias(libs.plugins.android.application)   // AGP 9+ : Kotlin intégré, plus de plugin kotlin.android
    alias(libs.plugins.kotlin.compose)
}

// --- Étape 7 : secrets de signature lus hors du script -----------------------
// local.properties (non versionné) en poste de développement,
// variables d'environnement en CI.
val proprietesLocales = Properties().apply {
    val fichier = rootProject.file("local.properties")
    if (fichier.exists()) fichier.inputStream().use { load(it) }
}

fun secret(cle: String, variable: String): String? =
    proprietesLocales.getProperty(cle) ?: providers.environmentVariable(variable).orNull

val signatureStoreFile = secret("signing.storeFile", "OUSUISJE_STORE_FILE")
val signatureStorePassword = secret("signing.storePassword", "OUSUISJE_STORE_PASSWORD")
val signatureKeyAlias = secret("signing.keyAlias", "OUSUISJE_KEY_ALIAS")
val signatureKeyPassword = secret("signing.keyPassword", "OUSUISJE_KEY_PASSWORD")

// Repli : sans les quatre valeurs (ou si le fichier n'existe pas), le build
// release reste possible mais produit un APK/AAB NON signé. Le debug n'en dépend jamais.
val signatureDisponible =
    listOf(signatureStoreFile, signatureStorePassword, signatureKeyAlias, signatureKeyPassword)
        .all { !it.isNullOrBlank() } &&
        rootProject.file(signatureStoreFile!!).exists()

android {
    namespace = "fr.formation.ousuisje"       // package de R et de BuildConfig (fixe)
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "fr.formation.ousuisje"   // modifié par les flavors (suffixes)
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (signatureDisponible) {
            create("release") {
                storeFile = rootProject.file(signatureStoreFile!!)
                storePassword = signatureStorePassword
                keyAlias = signatureKeyAlias
                keyPassword = signatureKeyPassword
            }
        }
    }

    // --- Étape 5 + bonus : deux dimensions de flavors --------------------------
    // Variantes = offre × environnement × build type, ex. proProdRelease
    flavorDimensions += listOf("offre", "environnement")

    productFlavors {
        create("gratuit") {
            dimension = "offre"
            applicationIdSuffix = ".gratuit"
            buildConfigField("Boolean", "SUIVI_CONTINU", "false")
            buildConfigField("long", "INTERVALLE_SUIVI_MS", "0L")
            resValue("string", "app_name", "Où suis-je ?")
        }
        create("pro") {
            dimension = "offre"
            buildConfigField("Boolean", "SUIVI_CONTINU", "true")
            buildConfigField("long", "INTERVALLE_SUIVI_MS", "10000L")
            resValue("string", "app_name", "Où suis-je ? Pro")
        }
        create("dev") {
            dimension = "environnement"
            applicationIdSuffix = ".dev"           // s'ajoute après celui de l'offre
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENVIRONNEMENT", "\"dev\"")
        }
        create("prod") {
            dimension = "environnement"
            buildConfigField("String", "ENVIRONNEMENT", "\"prod\"")
        }
    }

    buildTypes {
        release {
            // AGP 9 : remplace isMinifyEnabled / proguardFiles.
            // Les règles R8 du projet sont dans src/main/keepRules/.
            optimization {
                enable = true
                // R8 limité à ces paquets (choix du modèle AGP 9) ; on y ajoute le code de l'app
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**", "fr.formation.ousuisje.**")
            }
            if (signatureDisponible) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.warn("OuSuisJe : propriétés signing.* absentes de local.properties, release non signée.")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true      // désactivé par défaut depuis AGP 8
        resValues = true        // nécessaire pour resValue(...)
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
