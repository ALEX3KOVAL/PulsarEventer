plugins {
    kotlin("jvm")
}

group = "ru.alex3koval.eventer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(libs.kotlinx.serialization)

    implementation(libs.postgres)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.javaTime)
    implementation(project(":app"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}