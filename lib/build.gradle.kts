import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

val moduleName = "lib"
tasks {
    compileJava {
        inputs.property("moduleName", moduleName)
//        inputs.files(sourceSets.main.map { it.output.files })
        options.compilerArgumentProviders.add(CommandLineArgumentProvider {
            listOf(
                "--patch-module", "$moduleName=${sourceSets.main.get().output.asPath}"
            )
        })
    }
}
