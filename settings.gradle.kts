pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "uds-windows-kotlin"

val tempfileKotlinLocal = file("../tempfile-kotlin")
if (tempfileKotlinLocal.exists()) {
    includeBuild(tempfileKotlinLocal) {
        dependencySubstitution {
            substitute(module("io.github.kotlinmania:tempfile-kotlin"))
                .using(project(":"))
        }
    }
}
