package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion
import java.util.zip.ZipFile

/**
 * @author Silvio Giebl
 */
class RetroFixPlugin : Plugin<Project> {

    private var androidExtension: BaseExtension? = null

    @Override
    override fun apply(project: Project) {
        project.plugins.withType<BasePlugin> {

            val configuration = project.configurations.create("retrofix")
            project.configurations.named("implementation") {
                extendsFrom(configuration)
            }

            if (GradleVersion.current() >= GradleVersion.version("7.2")) {
                val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
                androidComponents.onVariants { applicationVariant ->
                    applicationVariant.instrumentation.transformClassesWith(
                        RetroFixClassVisitorFactory::class.java,
                        InstrumentationScope.ALL,
                    ) { parameters ->
                        parameters.classList.set(classListProvider(project, configuration))
                    }
                }
            } else if (GradleVersion.current() >= GradleVersion.version("7.0")) {
                val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
                androidComponents.onVariants { applicationVariant ->
                    applicationVariant.transformClassesWith(
                        RetroFixClassVisitorFactory::class.java,
                        InstrumentationScope.ALL,
                    ) { parameters ->
                        parameters.classList.set(classListProvider(project, configuration))
                    }
                }
            } else {
                val android = project.extensions.getByName("android") as BaseExtension
                android.registerTransform(RetroFixTransform(android))
                androidExtension = android
            }
        }

//        project.afterEvaluate {
//            val androidExtension = androidExtension
//                ?: throw GradleException("The RetroFix plugin requires the 'com.android.application' plugin.")
//            if (androidExtension.defaultConfig.minSdkVersion!!.apiLevel >= 24) {
//                throw GradleException("The RetroFix plugin should not be used when the minSdkVersion >= 24.")
//            }
//        }
    }
}

private fun classListProvider(project: Project, libraries: FileCollection) = project.provider {
    val classList = mutableListOf<String>()
    for (library in libraries) {
        if (!library.isFile || !library.name.endsWith(".jar")) {
            throw GradleException("libraries are expected to be only jar files, but found $library")
        }
        val zipFile = ZipFile(library)
        zipFile.stream().map { it.name.replace('\\', '/') }.forEach { name ->
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
