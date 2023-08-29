plugins {
    kotlin("jvm")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.mycompany.MainKt")
}

val moduleName = "app"
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
