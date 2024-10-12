import org.apache.tools.ant.filters.ReplaceTokens
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.birthdates"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.birthdates:services:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        // Remove -all from JAR file
        archiveClassifier.set("")
    }

    build {
        dependsOn("processYaml")
        finalizedBy({
            val tempDir = file("${layout.buildDirectory}/processedResources")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
                println("Temporary files deleted from ${tempDir.path}")
            }
        })
    }

    jar {
        from("${layout.buildDirectory}/processedResources") {
            include("plugin.yml")
        }
    }

    register("processYaml") {
        val gitCommitId = "git rev-parse --short HEAD".runCommand()
        val pluginFile = file("src/main/resources/plugin.yml") // Original file path

        doLast {
            // Read the original file content
            val originalContent = pluginFile.readText()

            // Modify the content as needed (e.g., replace a placeholder)
            val updatedContent = originalContent.replace("{projectVer}", gitCommitId)

            // Write the updated content to a temporary file in the build directory
            val tempPluginFile = file("${layout.buildDirectory}/processedResources/plugin.yml")
            tempPluginFile.parentFile.mkdirs() // Ensure the directory exists
            tempPluginFile.writeText(updatedContent)


            println("Processed $tempPluginFile with project version: $gitCommitId")
        }
    }
}

// Helper function to run shell commands
fun String.runCommand(): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine = this@runCommand.split(" ")
        standardOutput = output
    }
    return output.toString().trim()
}