package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.transform.*
import com.android.build.gradle.BaseExtension
import com.github.sgtsilvio.gradle.android.retrofix.backport.Backport
import com.github.sgtsilvio.gradle.android.retrofix.backport.FutureBackport
import com.github.sgtsilvio.gradle.android.retrofix.backport.StreamsBackport
import com.github.sgtsilvio.gradle.android.retrofix.backport.TimeBackport
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import com.google.common.collect.ImmutableSet
import javassist.ClassPool
import javassist.Modifier
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
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

    override fun getName() = "androidRetroFix"

    override fun getInputTypes() = ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)

    override fun getScopes() = ImmutableSet.of(
        QualifiedContent.Scope.PROJECT,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES,
    )

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation) {
        val backports = LinkedList<Backport>()
        backports.add(StreamsBackport())
        backports.add(FutureBackport())
        backports.add(TimeBackport())
        val presentBackports = LinkedList<Backport>()
        val backportJars = LinkedList<JarInput>()

        val classPool = ClassPool()
        classPool.appendSystemPath()
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
                        true
                    } else false
                }
            }
            for (directoryInput in input.directoryInputs) {
                classPool.insertClassPath(directoryInput.file.absolutePath)
            }
        }

        val typeMap = TypeMap()
        val methodMap = MethodMap()
        for (backport in presentBackports) {
            backport.apply(typeMap, methodMap)
        }

        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        for (transformInput in transformInvocation.inputs) {
            for (directoryInput in transformInput.directoryInputs) {
                val outputDir = transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
                )

                if (outputDir.exists()) {
                    check(outputDir.deleteRecursively())
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
            for (jarInput in transformInput.jarInputs) {
                val outputDir = transformInvocation.outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.DIRECTORY
                )
                val outputJar = transformInvocation.outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )

                if ((jarInput.status == Status.NOTCHANGED) && outputJar.exists()) {
                    continue
                }
                if (backportJars.contains(jarInput)) {
                    Files.copy(jarInput.file.toPath(), outputJar.toPath())
                    continue
                }
                if (outputDir.exists()) {
                    check(outputDir.deleteRecursively())
                }
                if (outputJar.exists()) {
                    Files.delete(outputJar.toPath())
                }
                if (jarInput.status == Status.REMOVED) {
                    continue
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
                check(outputDir.deleteRecursively())
            }
        }
    }

    private fun isTransformableClass(fileName: String) =
        fileName.endsWith(".class") && !fileName.startsWith("META-INF/") && !fileName.startsWith("META-INF\\") &&
                (fileName != "module-info.class") && (fileName != "package-info.class")

    private fun transformClass(
        classPool: ClassPool,
        className: String,
        typeMap: TypeMap,
        methodMap: MethodMap,
        outputDir: File,
    ) {
        logger.info("Transforming class {}", className)
        val ctClass = classPool.get(className)

        val replaceMap = HashMap<Int, String>()

//        ctClass.instrument(methodEditor { m, c ->
//            val key = m.methodName + " " + m.signature
//            var methodMapEntry = methodMap[key]
//            while (methodMapEntry != null) {
//                val method = m.method
//                val declaringClass = method.declaringClass
//                val matches = if (Modifier.isStatic(method.modifiers)) {
//                    declaringClass.name.equals(methodMapEntry.type)
//                } else {
//                    declaringClass.subtypeOf(declaringClass.classPool.get(methodMapEntry.type))
//                }
//                if (matches) {
//                    replaceMap[c] = methodMapEntry.replacement
//                    break
//                }
//                methodMapEntry = methodMapEntry.next
//            }
//        })

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

private fun methodEditor(consumer: (MethodCall, Int) -> Unit) = object : ExprEditor() {
    private var c = 0

    override fun edit(m: MethodCall) {
        try {
            consumer.invoke(m, c)
            c++
        } catch (throwable: Throwable) {
            throw RuntimeException(throwable)
        }
    }
}
