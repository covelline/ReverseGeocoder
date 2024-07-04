import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.ktfmt.gradle)
}
android {
    namespace = "com.covelline.reversegeocoder"
    compileSdk = 35
    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    jvmToolchain(11)
    androidTarget()
    jvm()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "GsiFeatureDB"
            isStatic = true
        }
    }
    sourceSets {
        val commonMain by getting {
            kotlin.setSrcDirs(
                listOf(
                    "src/commonMain/kotlin",
                )
            )
            dependencies {
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val iosX64Main by getting {
//            kotlin.srcDirs += setOf(file("build/generated/ksp/iOSX64/iOSX64/main/kotlin"))
        }
        val iosArm64Main by getting {
//            kotlin.srcDirs += setOf(file("build/generated/ksp/iOSArm64/iOSArm64/main/kotlin"))
        }
        val iosSimulatorArm64Main by getting {
//            kotlin.srcDirs += setOf(file("build/generated/ksp/iOSSimulatorArm64/iOSSimulatorArm64/main/kotlin"))
        }
        val iosMain by creating {
            kotlin.setSrcDirs(
                listOf(
                    "src/iosMain/kotlin",
                    "build/generated/ksp/metadata"
                )
            )
//            kotlin.srcDirs += setOf(file("build/generated/ksp/metadata/commonMain/kotlin"))
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val androidMain by getting {
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata" ) {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

dependencies {
    add("kspJvm", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
}

ktfmt {
    googleStyle()
}

val ktFmtFormat = tasks.register<KtfmtFormatTask>("ktFmtFormat") {
    source = project.fileTree("src")
    include("**/*.kt")
}

tasks.named("build") {
    dependsOn(ktFmtFormat)
}