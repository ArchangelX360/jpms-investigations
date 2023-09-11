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

kotlin {
    jvmToolchain(17)

    target {
        compilations {
            val other2 by registering {
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                }
            }

            val otherJar = tasks.register<Jar>("otherJar") {
                dependsOn(other2.get().compileTaskProvider)
                archiveAppendix.set("other")
                from(other2.get().output.classesDirs)
            }

            val main by getting {
                dependencies {
                    compileOnly(other2.get().compileDependencyFiles)
                    compileOnly(files(otherJar))
                    // Works if the following is uncommented:
                    // compileOnly(other2.get().output.classesDirs)
                }
            }
        }
    }
}
