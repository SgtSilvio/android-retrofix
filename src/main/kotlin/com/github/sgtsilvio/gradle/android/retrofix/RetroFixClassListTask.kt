package com.github.sgtsilvio.gradle.android.retrofix

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.zip.ZipFile

/**
 * @author Silvio Giebl
 */
abstract class RetroFixClassListTask : DefaultTask() {

    @get:InputFiles
    val libraries: ConfigurableFileCollection = project.objects.fileCollection()

    @get:OutputFile
    val classListFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    protected fun run() {
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
        classListFile.get().asFile.writeText(classList.joinToString("\n"))
    }
}