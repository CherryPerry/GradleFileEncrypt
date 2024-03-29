import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.5.31"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.github.ben-manes.versions") version "0.42.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
}

group = "com.cherryperry.gfe"
version = "2.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        apiVersion = "1.3"
    }
}

detekt {
    config = files("detekt.yml")
    baseline = file("detekt-baseline.xml")
}

gradlePlugin {
    plugins.register("gradleFileEncryptPlugin") {
        id = "com.cherryperry.gradle-file-encrypt"
        displayName = "Gradle file encrypt"
        description = "Simply encrypt your sensitive data in repository with password"
        implementationClass = "com.cherryperry.gfe.FileEncryptPlugin"
    }
}

pluginBundle {
    website = "https://github.com/CherryPerry/GradleFileEncrypt"
    vcsUrl = "https://github.com/CherryPerry/GradleFileEncrypt.git"
    tags = listOf("encryption", "cryptography")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit", "org.eclipse.jgit", "6.1.0.202203080745-r")
    compileOnly(gradleApi())
    testImplementation("junit", "junit", "4.13.2")
    testImplementation(gradleTestKit())
    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.20.0")
}
