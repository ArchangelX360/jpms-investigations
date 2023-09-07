import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation

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

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)

    target {
        compilations {
            val other2 by registering {
                val c = this
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                }
                compileJavaTaskProvider.configure {
                    options.compilerArgumentProviders.add(
                        JPMSPatch(project.sourceSets, c)
                    )
                }
            }

            val otherJar = tasks.register<Jar>("otherJar") {
                dependsOn(other2.get().compileTaskProvider)
                archiveAppendix.set("other")
                from(other2.get().output.classesDirs)
            }

            val main2 by registering {
                dependencies {
                    compileOnly(other2.get().compileDependencyFiles)
                    compileOnly(files(otherJar))
                }
                compileJavaTaskProvider.configure {
                    options.compilerArgumentProviders.add(
                        JPMSPatch(project.sourceSets, this@registering)
                    )
                }
            }
        }
    }
}

class JPMSPatch @Inject constructor(
    sourceSetContainer: SourceSetContainer,
    compilation: KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>,
) : CommandLineArgumentProvider {
    @get:CompileClasspath
    val compileClasspath: FileCollection = compilation.javaSourceSet.compileClasspath

    // @get:CompileClasspath but cyclic dep
    @get:Internal
    val compiledClasses: FileCollection = sourceSetContainer.getByName(compilation.name).output

    @get:CompileClasspath
    val sources: FileCollection = compilation.javaSourceSet.allSource

    override fun asArguments(): MutableIterable<String> = mutableListOf(
        "--module-path", compileClasspath.asPath, // avoids transitive readability issues
        "--patch-module", "${getJavaModuleName(sources.files)}=${compiledClasses.asPath}", // avoids "package is empty or does not exist"
    )
}

fun getJavaModuleName(sources: Set<File>): String {
    val moduleInfoFiles = sources.filter { file -> file.name == "module-info.java" }
    val moduleInfoFile = when {
        moduleInfoFiles.size == 0 -> throw StopExecutionException("no module-info.java file found")
        moduleInfoFiles.size > 1 -> error("one source set cannot have more than one module-info.java")
        else -> moduleInfoFiles.first()
    }
    // in real life, this is parsed properly
    return moduleInfoFile.readLines().first().split(" ")[1]
}
