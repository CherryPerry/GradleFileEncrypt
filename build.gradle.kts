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
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC7-3"
}

group = "com.cherryperry"
version = "0"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

detekt {
    profile("main", Action {
        config = file("detekt.yml")
    })
}

tasks.findByName("check")?.dependsOn("detektCheck")

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("junit:junit:4.12")
}
