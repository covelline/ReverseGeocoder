import com.github.jk1.license.render.SimpleHtmlReportRenderer
import com.github.jk1.license.task.ReportTask
import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.ktfmt.gradle)
    alias(libs.plugins.google.services)
    alias(libs.plugins.room)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.jk1.licenseReport)
}

android {
    namespace = "com.covelline.reversegeocoder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.covelline.reversegeocoder"
        minSdk = 30
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    flavorDimensionList += "environment"
    productFlavors {
        create("oss") {
            dimension = "environment"
        }
        create("development")  {
            dimension = "environment"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    assetPacks += listOf(":japan_administrative_divisions")
    sourceSets {
        named("main") {
            val generateTask = tasks.named("generateLicenseReport", ReportTask::class)
            assets.srcDirs(listOf(generateTask.get().outputFolder))
        }
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.productFlavors.contains("environment" to "oss")) {
            variantBuilder.enable = variantBuilder.buildType == "debug"
        }
    }
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val variantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            val proto = "generate${variantName}Proto"
            val ksp = "ksp${variantName}Kotlin"

            val protoTask = project.tasks.findByName(proto)
                    as? com.google.protobuf.gradle.GenerateProtoTask
            val kspTask = project.tasks.findByName(ksp)
                    as? org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>
            kspTask?.run {
                protoTask?.let {
                    @Suppress("DEPRECATION")
                    setSource(it.outputSourceDirectorySet)
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work)
    implementation(libs.androidx.hilt.work)
    implementation(libs.lottie.compose)
    implementation(libs.google.play.location)
    implementation(libs.dagger.hilt)
    implementation(libs.google.protobuf)
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.locationdelegation)
    implementation(project(":gsi_feature_database"))
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.datastore)
    implementation(libs.accompanist.permission)
    implementation(libs.kotlinx.googleplay)
    implementation(libs.timber)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.navigation.compose)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.locationtech.jts)
    implementation(libs.locationtech.jts.io)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin")
                create("java")
            }
        }
    }
}

ktfmt {
    googleStyle()
}

val ktFmtFormat = tasks.register<KtfmtFormatTask>("ktFmtFormat") {
    source = project.fileTree("src")
    include("**/*.kt")
}

licenseReport {
    renderers = arrayOf(SimpleHtmlReportRenderer("licenses.html"))
    allowedLicensesFile = project.layout.projectDirectory.file("config/allowed-licenses.json").asFile
    projects = arrayOf(project, project(":gsi_feature_database"))
}

// いくつかのファイルが生成されるので、index.json以外は消す
val cleanLicenseReport = tasks.register("cleanLicenseReport", DefaultTask::class) {
    dependsOn(tasks.generateLicenseReport)
    dependsOn(tasks.checkLicense)
    doLast {
        val output = tasks.generateLicenseReport.get().outputFolder
        output.listFiles()?.forEach { file ->
            if (file.name != "licenses.html") {
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(ktFmtFormat)
    dependsOn(cleanLicenseReport)
}
