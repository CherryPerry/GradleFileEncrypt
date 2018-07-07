import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.2.50"
    id("java-gradle-plugin")
    id("org.jmailen.kotlinter") version "1.14.0"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC7-3"
    id("com.gradle.plugin-publish") version "0.9.10"
}

group = "com.cherryperry.gfe"
version = "1.1.0"

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

pluginBundle {
    website = "https://github.com/CherryPerry/GradleFileEncrypt"
    vcsUrl = "https://github.com/CherryPerry/GradleFileEncrypt.git"
    description = "Simply encrypt your sensitive data in repository with password"
    tags = listOf("encryption", "cryptography")

    (plugins) {
        "gradleFileEncryptPlugin" {
            id = "com.cherryperry.gradle-file-encrypt"
            displayName = "Gradle file encrypt"
        }
    }
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
    testImplementation(gradleTestKit())
}
