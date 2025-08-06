plugins {
    kotlin("jvm") version "2.2.0"
    java
    `maven-publish`
}

group = "net.botwithus.xapi"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
    jvmToolchain(24)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        setUrl("https://nexus.botwithus.net/repository/maven-snapshots/")
    }
}

dependencies {
    implementation("net.botwithus.api:api:1.0.4-SNAPSHOT")
    implementation("net.botwithus.imgui:imgui:1.0.1-SNAPSHOT")
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Logging dependencies
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("ch.qos.logback:logback-core:1.4.11")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}