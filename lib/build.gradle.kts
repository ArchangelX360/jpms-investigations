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
            val other by registering {
                val c = this
                dependencies {
                    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                }
                compileJavaTaskProvider.configure {

                    // `hackDestinationDirectory` is used to prevent:
                    //
                    //   > Task :lib:compileKotlin FAILED
                    //   e: file:///.../jpms-test-stuff/lib/src/main/kotlin/org/mycompany2/utils.kt:5:23 Symbol is declared in unnamed module which is not read by current module
                    //   e: file:///.../jpms-test-stuff/lib/src/main/kotlin/org/mycompany2/utils.kt:7:23 Symbol is declared in unnamed module which is not read by current module
                    // ----------------------
                    // TODO: uncomment following line
                    // hackDestinationDirectory(c)
                    options.compilerArgumentProviders.add(
                        JPMSPatch(project.sourceSets, c)
                    )
                }
            }

            val main by getting {
                val c = this
                dependencies {
                    val o = other.get()
                    compileOnly(o.compileDependencyFiles + o.output.classesDirs)
                }
                compileJavaTaskProvider.configure {
                    // hackDestinationDirectory(c)
                    options.compilerArgumentProviders.add(
                        JPMSPatch(project.sourceSets, c)
                    )
                }
            }
        }
    }
}

fun JavaCompile.hackDestinationDirectory(compilation: KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>) {
    destinationDirectory.set(project.layout.dir(project.provider { compilation.output.classesDirs.files.first { it.path.contains("kotlin") } }))
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
