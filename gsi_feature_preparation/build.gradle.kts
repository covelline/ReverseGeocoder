plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":gsi_feature_database"))
    implementation(libs.kotlinx.atmicfu)
    implementation(libs.locationtech.jts)
    implementation(libs.locationtech.jts.io)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite)
    implementation(libs.kotlinx.coroutines.core)
}