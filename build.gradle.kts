plugins {
    `kotlin-dsl`
    signing
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.defaults)
    alias(libs.plugins.metadata)
}

group = "com.github.sgtsilvio.gradle"
description = "Backports Java 8 APIs (java.util.Optional, java.util.function, java.util.stream, " +
        "java.util.concurrent.CompletableFuture, java.time) to Android below API 24 (Android 7.0 Nougat)"

metadata {
    readableName = "Android RetroFix"
    license {
        apache2()
    }
    developers {
        register("SgtSilvio") {
            fullName = "Silvio Giebl"
        }
    }
    github {
        org = "SgtSilvio"
        issues()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.android.gradle.api)
    implementation(libs.asm.commons)
}

gradlePlugin {
    website = metadata.url
    vcsUrl = metadata.scm.get().url
    plugins {
        create("androidRetrofix") {
            id = "$group.android-retrofix"
            implementationClass = "$group.android.retrofix.RetroFixPlugin"
            displayName = metadata.readableName.get()
            description = project.description
            tags = listOf("android", "retrofit", "backport")
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}
