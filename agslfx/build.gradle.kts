import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "com.mikhailov.agslfx"
    compileSdk = 37

    defaultConfig {
        minSdk = 33
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            // AgslProgram ссылается на android.graphics.RuntimeShader в сигнатуре create().
            // Заглушки android.jar это переживают, пока тест не вызывает сам create().
            isReturnDefaultValues = true
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.foundation)
    api(libs.androidx.animation.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.mikhailov.agslfx"
            artifactId = "agsl-fx"
            version = "1.0.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}

/**
 * Компилирует все AGSL-шейдеры библиотеки настоящим компилятором SkSL до сборки APK.
 * Требует python3 и skia-python:  pip install skia-python
 */
val validateAgsl = tasks.register<Exec>("validateAgsl") {
    group = "verification"
    description = "Компилирует все AGSL-шейдеры библиотеки (нужен skia-python)"
    val python = if (System.getProperty("os.name").startsWith("Windows")) "python" else "python3"
    commandLine(python, rootProject.file("tools/validate_agsl.py").absolutePath)
    isIgnoreExitValue = false

    // Шейдеры меняются только в исходниках — без этого задача гоняется на каждый check.
    inputs.dir(layout.projectDirectory.dir("src/main/kotlin"))
    inputs.file(rootProject.file("tools/validate_agsl.py"))
    outputs.upToDateWhen { true }
}

// Локально skia-python есть не у всех, и заваливать этим сборку невежливо.
// На CI переменная выставлена, и валидация обязательна.
if (providers.environmentVariable("CI").isPresent) {
    tasks.named("check") { dependsOn(validateAgsl) }
}
