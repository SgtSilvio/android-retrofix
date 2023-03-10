plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish")
    id("io.github.sgtsilvio.gradle.defaults")
    id("com.github.sgtsilvio.gradle.metadata")
}

group = "com.github.sgtsilvio.gradle"
description = "Backports Java 8 APIs (java.util.Optional, java.util.function, java.util.stream, " +
        "java.util.concurrent.CompletableFuture, java.time) to Android below API 24 (Android 7.0 Nougat)"

metadata {
    readableName.set("Android RetroFix")
    license {
        apache2()
    }
    developers {
        register("SgtSilvio") {
            fullName.set("Silvio Giebl")
        }
    }
    github {
        org.set("SgtSilvio")
        issues()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly("com.android.tools.build:gradle:${property("android.tools.build.version")}")
    compileOnly("com.android.tools.build:gradle-api:${property("android.tools.build.version")}")
    implementation("org.ow2.asm:asm-commons:${property("asm.version")}")
}

gradlePlugin {
    website.set(metadata.url)
    vcsUrl.set(metadata.scm.get().url)
    plugins {
        create("android-retrofix") {
            id = "$group.$name"
            implementationClass = "$group.android.retrofix.RetroFixPlugin"
            displayName = metadata.readableName.get()
            description = project.description
            tags.set(listOf("android", "retrofit", "backport"))
        }
    }
}
