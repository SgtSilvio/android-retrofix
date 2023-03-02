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
    implementation("com.google.guava:guava:${property("guava.version")}")
    implementation("org.javassist:javassist:${property("javassist.version")}")
    implementation("org.jetbrains:annotations:${property("annotations.version")}")
}

gradlePlugin {
    plugins {
        create("android-retrofix") {
            id = "$group.$name"
            implementationClass = "$group.android.retrofix.RetroFixPlugin"
            displayName = metadata.readableName.get()
            description = project.description
        }
    }
}

pluginBundle {
    website = "https://github.com/SgtSilvio/android-retrofix"
    vcsUrl = "https://github.com/SgtSilvio/android-retrofix"
    tags = listOf("android", "retrofit", "backport")
}
