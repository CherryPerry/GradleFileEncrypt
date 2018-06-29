import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    java
    kotlin("jvm") version "1.2.50"
    id("java-gradle-plugin")
    id("org.jmailen.kotlinter") version "1.13.0"
    id("com.github.ben-manes.versions") version "0.20.0"
}

group = "com.cherryperry"
version = "0"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.mockito:mockito-core:2.19.0")
    testImplementation("junit:junit:4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}