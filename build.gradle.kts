plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
}

group = "ru.alex3koval.eventer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(libs.icu4j)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines)
    runtimeOnly(libs.pulsar.client)
    api(libs.pulsar.client.api)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}