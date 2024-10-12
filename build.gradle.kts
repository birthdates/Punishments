import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.palantir.git-version") version "3.1.0"
}

group = "com.birthdates"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(files("libs/services-1.0.0.jar"))
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        // Remove -all from JAR file
        archiveClassifier.set("")
    }
}

fun getCurrentGitCommitShortId(): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = output
    }
    return output.toString().trim()
}

tasks.processResources {
    val commitId = getCurrentGitCommitShortId()
    filesMatching("plugin.yml") {
        filter { line ->
            line.replace("\${version}", commitId)
        }
    }
}