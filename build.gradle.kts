import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.5.31"
    id("java-gradle-plugin")
    id("com.github.ben-manes.versions") version "0.28.0"
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.cherryperry.gfe"
version = "1.4.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        kotlinOptions.languageVersion = "1.3"
    }
}

detekt {
    config = files("detekt.yml")
}

pluginBundle {
    website = "https://github.com/CherryPerry/GradleFileEncrypt"
    vcsUrl = "https://github.com/CherryPerry/GradleFileEncrypt.git"
    description = "Simply encrypt your sensitive data in repository with password"
    tags = listOf("encryption", "cryptography")

    plugins.create("gradleFileEncryptPlugin") {
        id = "com.cherryperry.gradle-file-encrypt"
        displayName = "Gradle file encrypt"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit", "org.eclipse.jgit", "5.4.0.201906121030-r")
    compileOnly(gradleApi())
    testImplementation("junit", "junit", "4.12")
    testImplementation(gradleTestKit())
    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.18.1")
}
