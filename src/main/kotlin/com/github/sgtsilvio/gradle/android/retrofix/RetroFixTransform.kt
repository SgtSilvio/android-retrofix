package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.transform.*
import com.android.build.gradle.BaseExtension
import com.github.sgtsilvio.gradle.android.retrofix.backport.Backport
import com.github.sgtsilvio.gradle.android.retrofix.backport.FutureBackport
import com.github.sgtsilvio.gradle.android.retrofix.backport.StreamsBackport
import com.github.sgtsilvio.gradle.android.retrofix.backport.TimeBackport
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import com.github.sgtsilvio.gradle.android.retrofix.util.methodEditor
import com.google.common.collect.ImmutableSet
import javassist.ClassPool
import javassist.Modifier
import javassist.NotFoundException
import org.apache.commons.io.FileUtils
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.Zip
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @author Silvio Giebl
 */
class RetroFixTransform(private val androidExtension: BaseExtension) : Transform() {

    companion object {
        private val logger = LoggerFactory.getLogger(RetroFixTransform::class.java)
    }

    override fun getName(): String {
        return "androidRetroFix"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation) {
        val backports = LinkedList<Backport>()
        backports.add(StreamsBackport())
        backports.add(FutureBackport())
        backports.add(TimeBackport())
        val presentBackports = LinkedList<Backport>()
        val backportJars = LinkedList<JarInput>()

        val classPool = ClassPool()
        classPool.appendSystemPath()
        try {
            for (file in androidExtension.bootClasspath) {
                classPool.insertClassPath(file.absolutePath)
            }
            for (input in transformInvocation.inputs) {
                for (jarInput in input.jarInputs) {
                    classPool.insertClassPath(jarInput.file.absolutePath)
                    backports.removeIf { backport ->
                        if (backport.isPresent(classPool)) {
                            presentBackports.add(backport)
                            backportJars.add(jarInput)
                            return@removeIf true
                        }
                        false
                    }
                }
                for (directoryInput in input.directoryInputs) {
                    classPool.insertClassPath(directoryInput.file.absolutePath)
                }
            }
        } catch (e: NotFoundException) {
            throw RuntimeException(e)
        }

        val typeMap = TypeMap()
        val methodMap = MethodMap()
        presentBackports.forEach { backport -> backport.apply(typeMap, methodMap) }

        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        transformInvocation.inputs.forEach ignored@{ transformInput ->
            transformInput.directoryInputs.forEach { directoryInput ->
                val outputDir = transformInvocation.outputProvider.getContentLocation(
                        directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir)
                }
                if (!outputDir.mkdirs()) {
                    throw RuntimeException("Could not create output directory")
                }
                logger.info("Transforming directory {}", directoryInput.name)

                val inputPath = Paths.get(directoryInput.file.absolutePath)
                Files.walk(inputPath)
                        .filter(Files::isRegularFile)
                        .filter { path ->
                            if (isTransformableClass(path.fileName.toFile().name)) {
                                return@filter true
                            }
                            val filePath = inputPath.relativize(path).toString()
                            logger.info("Copying file {}", filePath)
                            val file = File(outputDir, filePath)
                            //noinspection ResultOfMethodCallIgnored
                            file.parentFile.mkdirs()
                            Files.copy(path, file.toPath())
                            false
                        }
                        .map(inputPath::relativize)
                        .map(Path::toString)
                        .map { s -> s.replace("/", ".").replace("\\\\", ".") }
                        .map { s -> s.substring(0, s.length - ".class".length) }
                        .forEach { s -> transformClass(classPool, s, typeMap, methodMap, outputDir) }
            }
            transformInput.jarInputs.forEach { jarInput ->
                val outputDir = transformInvocation.outputProvider.getContentLocation(
                        jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.DIRECTORY)
                val outputJar = transformInvocation.outputProvider.getContentLocation(
                        jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                if ((jarInput.status == Status.NOTCHANGED) && outputJar.exists()) {
                    return@forEach
                }
                if (backportJars.contains(jarInput)) {
                    FileUtils.copyFile(jarInput.file, outputJar)
                    return@forEach
                }
                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir)
                }
                if (outputJar.exists()) {
                    FileUtils.delete(outputJar)
                }
                if (jarInput.status == Status.REMOVED) {
                    return@forEach
                }
                if (!outputDir.mkdirs()) {
                    throw RuntimeException("Could not create output directory")
                }
                logger.info("Transforming jar {}", jarInput.name)

                val zipFile = ZipFile(jarInput.file)
                zipFile.stream()
                        .filter { entry -> !entry.isDirectory }
                        .filter { entry ->
                            if (isTransformableClass(entry.name)) {
                                return@filter true
                            }
                            logger.info("Copying file {}", entry.name)
                            val file = File(outputDir, entry.name)
                            //noinspection ResultOfMethodCallIgnored
                            file.parentFile.mkdirs()
                            Files.copy(zipFile.getInputStream(entry), file.toPath())
                            false
                        }
                        .map(ZipEntry::getName)
                        .map { s -> s.replace("/", ".").replace("\\\\", ".") }
                        .map { s -> s.substring(0, s.length - ".class".length) }
                        .forEach { s -> transformClass(classPool, s, typeMap, methodMap, outputDir) }

                zip(outputDir, outputJar)
                FileUtils.deleteDirectory(outputDir)
            }
        }
    }

    private fun isTransformableClass(fileName: String): Boolean {
        return fileName.endsWith(".class") && !fileName.startsWith("META-INF/") && !fileName.startsWith("META-INF\\") &&
                fileName != "module-info.class" && fileName != "package-info.class"
    }

    private fun transformClass(
            classPool: ClassPool, className: String, typeMap: TypeMap, methodMap: MethodMap, outputDir: File) {

        logger.info("Transforming class {}", className)
        val ctClass = classPool.get(className)

        val replaceMap = HashMap<Int, String>()

        ctClass.instrument(methodEditor { m, c ->
            val key = m.methodName + " " + m.signature
            var methodMapEntry = methodMap[key]
            while (methodMapEntry != null) {
                val method = m.method
                val declaringClass = method.declaringClass
                val matches = if (Modifier.isStatic(method.modifiers)) {
                    declaringClass.name.equals(methodMapEntry.type)
                } else {
                    declaringClass.subtypeOf(declaringClass.classPool.get(methodMapEntry.type))
                }
                if (matches) {
                    replaceMap[c] = methodMapEntry.replacement
                    break
                }
                methodMapEntry = methodMapEntry.next
            }
        })

        ctClass.classFile.renameClass(typeMap)

        ctClass.instrument(methodEditor { m, c ->
            val replacement = replaceMap[c]
            if (replacement != null) {
                m.replace(replacement)
            }
        })

        ctClass.classFile.compact()

        ctClass.writeFile(outputDir.absolutePath)
    }

    private fun zip(inputDir: File, outputFile: File) {
        val p = Project()
        p.init()
        val zip = Zip()
        zip.project = p
        zip.destFile = outputFile
        zip.setBasedir(inputDir)
        zip.perform()
    }
}
