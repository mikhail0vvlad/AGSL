import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Ключ подписи в репозиторий не попадает. Чтобы собрать подписанный release,
// положите keystore куда удобно и опишите его в local.properties (файл в .gitignore):
//
//   signing.storeFile=demo/demo-signing.jks
//   signing.storePassword=...
//   signing.keyAlias=demo
//   signing.keyPassword=...
//
// Без этих строк release собирается неподписанным, а debug — обычным отладочным
// ключом Android SDK, то есть витрину всё равно можно поставить на телефон.
val signingProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val signingStore = signingProps.getProperty("signing.storeFile")
    ?.let(rootProject::file)
    ?.takeIf { it.exists() }

android {
    namespace = "com.mikhailov.agslfx.demo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mikhailov.agslfx.demo"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (signingStore != null) {
            create("demo") {
                storeFile = signingStore
                storePassword = signingProps.getProperty("signing.storePassword")
                keyAlias = signingProps.getProperty("signing.keyAlias")
                keyPassword = signingProps.getProperty("signing.keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            if (signingStore != null) {
                signingConfig = signingConfigs.getByName("demo")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (signingStore != null) {
                signingConfig = signingConfigs.getByName("demo")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":agslfx"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}
