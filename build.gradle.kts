plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
}

group = "com.github.sgtsilvio.gradle"
description = "Backports Java 8 APIs (java.util.Optional, java.util.function, java.util.stream, " +
        "java.util.concurrent.CompletableFuture) to Android APIs < 24 (Android 7.0 Nougat)"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:${property("android.tools.build.version")}")
    implementation("com.android.tools.build:gradle-api:${property("android.tools.build.version")}")
    implementation("org.javassist:javassist:${property("javassist.version")}")
    implementation("org.jetbrains:annotations:${property("annotations.version")}")
}

gradlePlugin {
    plugins {
        create("android-retrofix") {
            id = "$group.$name"
            displayName = "Android RetroFix"
            description = project.description
            implementationClass = "$group.android.retrofix.RetroFixPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/SgtSilvio/android-retrofix"
    vcsUrl = "https://github.com/SgtSilvio/android-retrofix"
    tags = listOf("android", "retrofit", "backport")
}
