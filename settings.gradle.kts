rootProject.name = "android-retrofix"

pluginManagement {
    plugins {
        id("com.gradle.plugin-publish") version "${extra["plugin.plugin-publish.version"]}"
        id("io.github.sgtsilvio.gradle.defaults") version "${extra["plugin.defaults.version"]}"
        id("com.github.sgtsilvio.gradle.metadata") version "${extra["plugin.metadata.version"]}"
    }
}
