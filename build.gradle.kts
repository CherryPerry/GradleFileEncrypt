import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.50"
}

group = "com.cherryperry"
version = "0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(gradleApi())
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}