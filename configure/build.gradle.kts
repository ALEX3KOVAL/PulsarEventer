plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
}

group = "ru.alex3koval"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(libs.kaml)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}