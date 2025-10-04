import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
    signing
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.defaults)
    alias(libs.plugins.metadata)
}

group = "com.github.sgtsilvio.gradle"

metadata {
    readableName = "Android RetroFix"
    description = "Backports Java 8 APIs (java.util.Optional, java.util.function, java.util.stream, " +
            "java.util.concurrent.CompletableFuture, java.time) to Android below API 24 (Android 7.0 Nougat)"
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

kotlin {
    jvmToolchain(11)
}

tasks.compileKotlin {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
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
    plugins {
        create("androidRetrofix") {
            id = "$group.android-retrofix"
            implementationClass = "$group.android.retrofix.RetroFixPlugin"
            tags = listOf("android", "retrofit", "backport")
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}
