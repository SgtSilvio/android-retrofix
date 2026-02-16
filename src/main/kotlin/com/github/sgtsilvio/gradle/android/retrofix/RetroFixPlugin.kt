package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import java.util.zip.ZipFile

/**
 * @author Silvio Giebl
 */
@Suppress("unused")
class RetroFixPlugin : Plugin<Project> {

    @Override
    override fun apply(project: Project) {
        project.plugins.withType<BasePlugin> {

            val configuration = project.configurations.create("retrofix")
            project.configurations.named("implementation") {
                extendsFrom(configuration)
            }

            val androidComponents = project.extensions["androidComponents"] as AndroidComponentsExtension<*, *, *>
            androidComponents.onVariants { applicationVariant ->
                applicationVariant.instrumentation.transformClassesWith(
                    RetroFixClassVisitorFactory::class.java,
                    InstrumentationScope.ALL,
                ) { parameters ->
                    parameters.classList.set(classListProvider(project, configuration))
                }
            }
        }

        project.afterEvaluate {
            val androidExtension = project.extensions["android"] as? ApplicationExtension
                ?: throw GradleException("The RetroFix plugin requires the 'com.android.application' plugin.")
            if (androidExtension.defaultConfig.minSdk!! >= 24) {
                throw GradleException("The RetroFix plugin should not be used when minSdk >= 24.")
            }
        }
    }
}

private fun classListProvider(project: Project, libraries: FileCollection) = project.provider {
    val classList = mutableListOf<String>()
    for (library in libraries) {
        if (!library.isFile || !library.name.endsWith(".jar")) {
            throw GradleException("libraries are expected to be only jar files, but found $library")
        }
        ZipFile(library).stream().map { it.name.replace('\\', '/') }.forEach { name ->
            if (name.endsWith(".class") &&
                !name.startsWith("META-INF/") &&
                (name != "module-info.class") &&
                (name != "package-info.class")
            ) {
                classList.add(name.removeSuffix(".class"))
            }
        }
    }
    classList
}
