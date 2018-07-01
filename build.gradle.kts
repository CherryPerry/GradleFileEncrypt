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
    java
    kotlin("jvm") version "1.2.50"
    id("java-gradle-plugin")
    id("org.jmailen.kotlinter") version "1.14.0"
    id("com.github.ben-manes.versions") version "0.20.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC7-3"
    id("com.gradle.plugin-publish") version "0.9.10"
}

fun getHeadCommitSHA1Short(logger: Logger): String? {
    val gitDir = project.file(".git")
    if (!gitDir.exists() || !gitDir.isDirectory || !gitDir.canRead()) {
        logger.error(".git directory does not exist")
        return null
    }
    val headFile = File(gitDir, "HEAD")
    if (!headFile.exists() || !headFile.isFile || !headFile.canRead()) {
        logger.error("HEAD does not exist")
        return null
    }
    val headContent = headFile.readText(Charsets.UTF_8)
    if (headContent.contains(':')) {
        logger.warn("HEAD is branch")
        val refFile = File(gitDir, headContent.split(':')[1].trim())
        if (!refFile.exists() || !refFile.isFile || !refFile.canRead()) {
            logger.warn("Failed to find HEAD's branch")
            return null
        }
        return refFile.readText(Charsets.UTF_8).trim().take(7)
    } else {
        logger.warn("HEAD is single commit")
        return headContent.trim().take(7)
    }
}

val currentCommit = getHeadCommitSHA1Short(logger)
logger.warn("$currentCommit")
group = "com.cherryperry.gfe"
version = if (currentCommit != null) {
    "1.0.$currentCommit"
} else {
    throw IllegalStateException("Failed to resolve commit based version")
}

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
    compileOnly(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
}
