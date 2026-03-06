plugins {
    java
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "xapi"
        )
    }
}

group = "net.botwithus.xapi"
version = "2.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("../BWUJavaScriptingFramework/api/build/libs/api-1.0-SNAPSHOT.jar"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            url = layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            
            pom {
                name.set("BotWithUs XAPI")
                description.set("Extended API framework for BotWithUs RuneScape 3 bot development")
                
                properties.set(mapOf(
                    "maven.compiler.source" to "21",
                    "maven.compiler.target" to "21"
                ))
            }
        }
    }
}
