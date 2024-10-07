plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "me.ws"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.squareup.retrofit2:retrofit-bom:2.11.0"))
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization")
    implementation("com.squareup.retrofit2:converter-scalars")
    implementation("com.squareup.retrofit2:retrofit")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    testImplementation(kotlin("test"))
    implementation(libs.bundles.deps)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}