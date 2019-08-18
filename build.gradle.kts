import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.3.21"
    id("java-gradle-plugin")
    id("com.github.ben-manes.versions") version "0.21.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC14"
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.cherryperry.gfe"
version = "1.3.1"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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
    jcenter()
}

dependencies {
    implementation("org.eclipse.jgit", "org.eclipse.jgit", "5.4.0.201906121030-r")
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(gradleApi())
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
    testImplementation(gradleTestKit())
    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.0.0-RC14")
}
