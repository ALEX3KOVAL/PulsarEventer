plugins {
    alias(libs.plugins.jvm)
}

group = "ru.alex3koval"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(libs.icu4j)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.koin.core)
    implementation(libs.exposed.core)
    implementation(libs.hikari)

    runtimeOnly(libs.pulsar.client)
    api(libs.pulsar.client.api)

    implementation(project(":configure"))
    implementation(project(":storage"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}